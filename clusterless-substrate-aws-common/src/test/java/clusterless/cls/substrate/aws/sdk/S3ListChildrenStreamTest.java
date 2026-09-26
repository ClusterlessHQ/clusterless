/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.sdk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * A listing page that fails mid-stream must fail the stream with the listed path and the
 * cause, not end it early or escape as a bare SDK exception; consuming the stream in
 * try-with-resources must release the client the paginator holds.
 */
public class S3ListChildrenStreamTest {
    static final URI PATH = URI.create("s3://bucket/arcs/arc=arc1/");
    static final URI START = URI.create("s3://bucket/arcs/arc=arc1/lot=1");
    static final URI END = URI.create("s3://bucket/arcs/arc=arc1/lot=4");

    S3Client client;
    S3 s3;

    @BeforeEach
    void setUp() {
        client = mock(S3Client.class);
        when(client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
                .thenAnswer(invocation -> new ListObjectsV2Iterable(client, invocation.getArgument(0)));

        s3 = new S3() {
            @Override
            protected S3Client createClient(String region) {
                return client;
            }
        };
    }

    static ListObjectsV2Response page(boolean truncated, String... keys) {
        return (ListObjectsV2Response) ListObjectsV2Response.builder()
                .contents(Arrays.stream(keys).map(key -> S3Object.builder().key(key).build()).toList())
                .isTruncated(truncated)
                .nextContinuationToken(truncated ? "next" : null)
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();
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
    void failedPageThrowsWithPathAndCause() {
        when(client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(page(true, "arcs/arc=arc1/lot=1/complete.arc"))
                .thenThrow(accessDenied());

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            try (Stream<String> stream = s3.listChildrenStream(s3.listObjectsIterable(PATH, START), PATH, END, ".arc")) {
                stream.toList();
            }
        });

        Assertions.assertTrue(exception.getMessage().contains(PATH.toString()), exception.getMessage());
        Assertions.assertTrue(exception.getMessage().contains("Access Denied"), exception.getMessage());
        Assertions.assertInstanceOf(S3Exception.class, exception.getCause());
        verify(client).close();
    }

    @Test
    void closingStreamClosesClient() {
        when(client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(page(false, "arcs/arc=arc1/lot=1/complete.arc"));

        Stream<String> stream = s3.listChildrenStream(s3.listObjectsIterable(PATH, START), PATH, END, ".arc");

        Assertions.assertEquals(1, stream.toList().size());
        verify(client, never()).close();

        stream.close();

        verify(client).close();
    }

    @Test
    void multiPageSuccessReturnsKeysBeforeEndExclusive() {
        when(client.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(page(true, "arcs/arc=arc1/lot=1/complete.arc", "arcs/arc=arc1/lot=2/running.arc", "arcs/arc=arc1/lot=2/other.json"))
                .thenReturn(page(false, "arcs/arc=arc1/lot=3/complete.arc", "arcs/arc=arc1/lot=4/complete.arc"));

        List<String> keys;
        try (Stream<String> stream = s3.listChildrenStream(s3.listObjectsIterable(PATH, START), PATH, END, ".arc")) {
            keys = stream.toList();
        }

        Assertions.assertEquals(List.of(
                "arcs/arc=arc1/lot=1/complete.arc",
                "arcs/arc=arc1/lot=2/running.arc",
                "arcs/arc=arc1/lot=3/complete.arc"
        ), keys);
    }
}
