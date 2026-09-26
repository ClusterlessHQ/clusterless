/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.meta;

import clusterless.cls.json.JSONUtil;
import clusterless.cls.model.DeployableLoader;
import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.ProjectMaterialsURI;
import clusterless.cls.substrate.uri.ProjectURI;
import clusterless.cls.util.ExitCodeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Logging is off at the default verbosity, so a metadata failure after a successful cdk
 * deploy/destroy must be thrown as an {@link ExitCodeException} (printed on stderr by the
 * execution exception handler), not only logged with a non-zero return.
 */
public class MetadataFailureTest {
    @TempDir
    Path tempDir;

    List<Deployable> deployables;
    Deployable deployable;

    @BeforeEach
    void setUp() throws IOException {
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

        deployables = new DeployableLoader(List.of(project.toFile())).readObjects("aws");
        deployable = deployables.get(0);
    }

    @Test
    void pushFailureIsThrownNamingTheProjectURI() {
        URI projectURI = ProjectURI.builder()
                .withPlacement(deployable.placement())
                .withProject(deployable.project())
                .build()
                .uri();

        S3 s3 = mock(S3.class);
        when(s3.put(any(URI.class), anyString(), (Object) any())).thenReturn(failed());

        ExitCodeException exception = Assertions.assertThrows(
                ExitCodeException.class,
                () -> Metadata.pushDeployablesMetadata((p, r) -> s3, "profile", deployables, Collections.emptyList())
        );

        Assertions.assertEquals(1, exception.exitCode());
        Assertions.assertTrue(exception.getMessage().contains(projectURI.toString()), exception.getMessage());
        Assertions.assertTrue(exception.getMessage().contains("Access Denied"), exception.getMessage());
    }

    @Test
    void removeFailureIsThrownAfterAttemptingEveryRemoval() {
        URI materialsURI = ProjectMaterialsURI.builder()
                .withPlacement(deployable.placement())
                .withProject(deployable.project())
                .build()
                .uri();

        List<URI> materials = List.of(URI.create("s3://bucket/one.json"), URI.create("s3://bucket/two.json"));

        S3 s3 = mock(S3.class);
        S3.Response materialsResponse = new S3().new Response(ResponseBytes.fromByteArray(
                (GetObjectResponse) GetObjectResponse.builder().sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build()).build(),
                JSONUtil.writeAsStringSafe(materials).getBytes(StandardCharsets.UTF_8)
        ));

        when(s3.get(materialsURI)).thenReturn(materialsResponse);
        when(s3.exists(materialsResponse)).thenReturn(true);
        when(s3.remove(any(URI.class))).thenReturn(failed());

        ExitCodeException exception = Assertions.assertThrows(
                ExitCodeException.class,
                () -> Metadata.removeDeployablesMetadata((p, r) -> s3, "profile", deployables, Collections.emptyList())
        );

        Assertions.assertEquals(1, exception.exitCode());
        Assertions.assertTrue(exception.getMessage().contains(materialsURI.toString()), exception.getMessage());
        verify(s3, times(3)).remove(any(URI.class));
    }

    @Test
    void unreadableDeployMetadataIsThrownNamingThePath() {
        Path missing = tempDir.resolve("cdk.out");

        ExitCodeException exception = Assertions.assertThrows(
                ExitCodeException.class,
                () -> Metadata.deployablesMetadata(missing.toString(), false, (d, a) -> 0)
        );

        Assertions.assertEquals(1, exception.exitCode());
        Assertions.assertTrue(exception.getMessage().contains(Metadata.createProjectMetaPath(missing).toAbsolutePath().toString()), exception.getMessage());
    }

    @Test
    void dryRunSkipsReadingDeployMetadata() {
        Assertions.assertEquals(0, Metadata.deployablesMetadata(tempDir.resolve("cdk.out").toString(), true, (d, a) -> 1));
    }

    private static S3.Response failed() {
        return new S3().new Response(new IllegalStateException("Access Denied"));
    }
}
