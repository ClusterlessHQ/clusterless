/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report.scanner;

import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Project;
import clusterless.cls.substrate.aws.report.ArcRecord;
import clusterless.cls.substrate.aws.report.ArcStatusRecord;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.util.Moment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * {@link ArcStatusScanner#scannerOrNull} treats {@link IllegalStateException} as "no states, skip this arc",
 * so a failed state listing must surface as a different exception rather than silently dropping the arc
 * from the report.
 */
public class ScannerListingFailureTest {
    static final String PREFIX = "arcs/project=test-project/version=20230101/arc=arc1/";
    static final Moment EARLIEST = new Moment("now", Instant.parse("2021-11-12T00:00:00Z"));
    static final Moment LATEST = new Moment("now", Instant.parse("2021-11-12T00:20:00Z"));

    S3Client client;
    ArcRecord arcRecord;

    @BeforeEach
    void setUp() {
        client = mock(S3Client.class);
        when(client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
                .thenAnswer(invocation -> new ListObjectsV2Iterable(client, invocation.getArgument(0)));

        arcRecord = new ArcRecord(
                Placement.builder()
                        .withAccount("00000000")
                        .withRegion("us-west-2")
                        .withProvider("aws")
                        .withStage("prod")
                        .build(),
                Project.Builder.builder()
                        .withName("test-project")
                        .withVersion("20230101")
                        .build(),
                "arc1"
        );
    }

    ArcStatusScanner scanner(boolean fillGaps) {
        S3Client s3Client = client;
        return new ArcStatusScanner("default", arcRecord, EARLIEST, LATEST, Optional::empty, fillGaps) {
            @Override
            protected S3 createS3(int maxKeys) {
                return new S3() {
                    @Override
                    protected S3Client createClient(String region) {
                        return s3Client;
                    }
                };
            }
        };
    }

    static ListObjectsV2Response ok(ListObjectsV2Response.Builder builder) {
        return (ListObjectsV2Response) builder
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();
    }

    static ListObjectsV2Response lots(String... lots) {
        return ok(ListObjectsV2Response.builder()
                .commonPrefixes(Arrays.stream(lots).map(lot -> CommonPrefix.builder().prefix(PREFIX + "lot=" + lot + "/").build()).toList()));
    }

    static ListObjectsV2Response page(boolean truncated, String... keys) {
        return ok(ListObjectsV2Response.builder()
                .contents(Arrays.stream(keys).map(key -> S3Object.builder().key(PREFIX + key).build()).toList())
                .isTruncated(truncated)
                .nextContinuationToken(truncated ? "next" : null));
    }

    static S3Exception accessDenied() {
        return (S3Exception) S3Exception.builder()
                .statusCode(403)
                .message("Access Denied")
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode("AccessDenied")
                        .errorMessage("Access Denied")
                        .build())
                .build();
    }

    @Test
    void stateListingFailureIsNotNoStates() {
        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenThrow(accessDenied());

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> scanner(false));

        Assertions.assertFalse(exception instanceof IllegalStateException, "scannerOrNull would swallow: " + exception);
        Assertions.assertTrue(exception.getMessage().contains(PREFIX), exception.getMessage());
        Assertions.assertTrue(exception.getMessage().contains("Access Denied"), exception.getMessage());
    }

    @Test
    void emptyStatesIsSkipped() {
        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(lots());

        // the exception scannerOrNull catches to skip the arc
        Assertions.assertThrows(IllegalStateException.class, () -> scanner(false));
    }

    @Test
    void multiPageScanReturnsEveryLotBeforeEnd() {
        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenAnswer(invocation -> {
            ListObjectsV2Request request = invocation.getArgument(0);

            if (request.delimiter() != null) {
                return lots("20211112PT5M000");
            }

            if (request.continuationToken() == null) {
                return page(true, "lot=20211112PT5M000/complete.arc", "lot=20211112PT5M001/running.arc");
            }

            return page(false, "lot=20211112PT5M002/complete.arc", "lot=20211112PT5M003/complete.arc", "lot=20211112PT5M004/complete.arc");
        });

        List<String> lots;
        try (Stream<ArcStatusRecord> stream = scanner(false).scan()) {
            lots = stream.map(ArcStatusRecord::lotId).toList();
        }

        Assertions.assertEquals(List.of("20211112PT5M000", "20211112PT5M001", "20211112PT5M002", "20211112PT5M003"), lots);
    }

    @Test
    void scanPageFailureThrowsAndClosesClient() {
        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenAnswer(invocation -> {
            ListObjectsV2Request request = invocation.getArgument(0);

            if (request.delimiter() != null) {
                return lots("20211112PT5M000");
            }

            if (request.continuationToken() == null) {
                return page(true, "lot=20211112PT5M000/complete.arc");
            }

            throw accessDenied();
        });

        ArcStatusScanner scanner = scanner(true);
        clearInvocations(client);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            try (Stream<ArcStatusRecord> stream = scanner.scan()) {
                stream.toList();
            }
        });

        Assertions.assertTrue(exception.getMessage().contains(PREFIX), exception.getMessage());
        Assertions.assertInstanceOf(S3Exception.class, exception.getCause());
        verify(client).close();
    }
}
