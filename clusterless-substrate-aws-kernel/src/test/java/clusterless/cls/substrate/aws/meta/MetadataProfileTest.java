/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.meta;

import clusterless.cls.model.DeployableLoader;
import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.substrate.aws.cdk.CDKProcessExec;
import clusterless.cls.substrate.aws.sdk.S3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The metadata push runs in the parent process after cdk deploys, where CLS_CDK_PROFILE
 * (set only on the cdk synth child) is absent; it must use the command's profile.
 */
@ExtendWith(SystemStubsExtension.class)
public class MetadataProfileTest {
    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(CDKProcessExec.CLS_CDK_PROFILE, "wrong-profile"); // must not be consulted

    @TempDir
    Path tempDir;

    @Test
    void pushUsesTheCommandProfileAndPlacementRegion() throws IOException {
        Path project = tempDir.resolve("project.json");

        Files.writeString(project, """
                {
                  "project": { "name": "TestProject", "version": "20230101-00" },
                  "placement": { "stage": "prod", "provider": "aws", "account": "000000000000", "region": "us-east-2" },
                  "resources": [
                    { "type": "aws:core:s3Bucket", "name": "bucket", "bucketName": "sample-bucket" }
                  ]
                }
                """);

        List<Deployable> deployables = new DeployableLoader(List.of(project.toFile())).readObjects("aws");

        List<String> requested = new ArrayList<>();

        BiFunction<String, String, S3> s3For = (profile, region) -> {
            requested.add(profile + "@" + region);
            return acceptingS3();
        };

        int exitCode = Metadata.pushDeployablesMetadata(s3For, "command-profile", deployables, Collections.emptyList());

        Assertions.assertEquals(0, exitCode);
        Assertions.assertEquals(List.of("command-profile@us-east-2"), requested);
    }

    private static S3 acceptingS3() {
        S3.Response accepted = new S3().new Response((PutObjectResponse) PutObjectResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build());

        S3 s3 = mock(S3.class);

        when(s3.put(any(URI.class), anyString(), (Object) any())).thenReturn(accepted);

        return s3;
    }
}
