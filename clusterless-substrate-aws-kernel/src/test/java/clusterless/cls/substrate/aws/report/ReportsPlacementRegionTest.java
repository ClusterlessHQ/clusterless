/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Project;
import clusterless.cls.substrate.aws.report.scanner.ArcStatusScanner;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.util.Moment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Bootstrap buckets live in their placement's region and the SDK does not follow a cross-region
 * redirect, so every report listing must use the placement's region, not the environment's.
 */
@ExtendWith(SystemStubsExtension.class)
public class ReportsPlacementRegionTest {
    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set("AWS_REGION", "us-west-2"); // must not be consulted

    static Placement placement(String region) {
        return Placement.builder()
                .withAccount("000000000000")
                .withRegion(region)
                .withProvider("aws")
                .withStage("prod")
                .build();
    }

    @Test
    void arcListingsUseEachPlacementRegion() {
        List<String> requested = new ArrayList<>();
        List<String> listed = new ArrayList<>();

        Arcs arcs = new Arcs() {
            @Override
            protected List<Placement> listAllPlacements(String profile) {
                return List.of(placement("us-east-2"), placement("eu-west-1"));
            }
        };

        arcs.commandOptions.setProfile("p");
        arcs.s3For = (profile, region) -> {
            requested.add(profile + "@" + region);
            return listingS3(region, listed);
        };

        List<ArcRecord> records;
        try (Stream<ArcRecord> stream = arcs.listAllArcs()) {
            records = stream.toList();
        }

        Assertions.assertEquals(List.of("us-east-2", "eu-west-1"), records.stream().map(r -> r.placement().region()).toList());
        Assertions.assertEquals(List.of("p@us-east-2", "p@eu-west-1", "p@us-east-2", "p@eu-west-1"), requested);
        Assertions.assertEquals(4, listed.size());
        listed.forEach(entry -> {
            String[] split = entry.split(" ");
            Assertions.assertTrue(URI.create(split[1]).getHost().endsWith(split[0]), entry);
        });
    }

    /**
     * Returns one project or arc key, recording the region the client was built for and the listed uri.
     */
    private static S3 listingS3(String region, List<String> listed) {
        S3 s3 = mock(S3.class);

        when(s3.listAllChildren(any(URI.class))).thenAnswer(invocation -> {
            URI uri = invocation.getArgument(0);
            listed.add(region + " " + uri);

            if (uri.getPath().startsWith("/projects/")) {
                return List.of("projects/name=test-project/version=20230101/project.json");
            }

            return List.of("arcs/project=test-project/version=20230101/arc=arc1/arc.json");
        });

        return s3;
    }

    @Test
    void scannerListsInThePlacementRegion() {
        S3Client client = mock(S3Client.class);

        when(client.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
                .thenAnswer(invocation -> new ListObjectsV2Iterable(client, invocation.getArgument(0)));
        when(client.listObjectsV2(any(ListObjectsV2Request.class))).thenAnswer(invocation -> {
            ListObjectsV2Request request = invocation.getArgument(0);
            ListObjectsV2Response.Builder builder = ListObjectsV2Response.builder();

            if (request.delimiter() != null) {
                builder.commonPrefixes(CommonPrefix.builder().prefix(request.prefix() + "lot=20211112PT5M000/").build());
            }

            return builder.sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build()).build();
        });

        ArcRecord arcRecord = new ArcRecord(
                placement("eu-west-1"),
                Project.Builder.builder()
                        .withName("test-project")
                        .withVersion("20230101")
                        .build(),
                "arc1"
        );

        List<String> regions = new ArrayList<>();

        ArcStatusScanner scanner = new ArcStatusScanner(
                "p",
                arcRecord,
                new Moment("now", Instant.parse("2021-11-12T00:00:00Z")),
                new Moment("now", Instant.parse("2021-11-12T00:20:00Z")),
                Optional::empty,
                false
        ) {
            @Override
            protected S3 createS3(int maxKeys) {
                regions.add(super.createS3(maxKeys).region());

                return new S3() {
                    @Override
                    protected S3Client createClient(String region) {
                        return client;
                    }
                };
            }
        };

        try (Stream<ArcStatusRecord> stream = scanner.scan()) {
            stream.toList();
        }

        Assertions.assertEquals(List.of("eu-west-1", "eu-west-1"), regions);
    }
}
