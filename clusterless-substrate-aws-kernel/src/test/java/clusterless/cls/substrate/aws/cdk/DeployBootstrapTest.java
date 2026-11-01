/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk;

import clusterless.cls.model.deploy.Placement;
import clusterless.cls.substrate.aws.Kernel;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.util.ExitCodeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@code cls deploy --bootstrap} bootstraps any placement missing its bootstrap before deploying.
 */
@ExtendWith(SystemStubsExtension.class)
public class DeployBootstrapTest {
    @SystemStub
    SystemOut systemOut;

    @SystemStub
    SystemErr systemErr;

    @TempDir
    Path tempDir;

    static Placement placement(String stage) {
        return Placement.builder()
                .withAccount("000000000000")
                .withRegion("us-east-2")
                .withProvider("aws")
                .withStage(stage)
                .build();
    }

    @Test
    void bootstrapCommandIncludesStage() {
        Assertions.assertEquals(
                "cls bootstrap --account 000000000000 --region us-east-2 --stage prod",
                BaseCDKCommand.bootstrapCommandFor(placement("prod"))
        );
    }

    @Test
    void bootstrapCommandOmitsMissingStage() {
        Assertions.assertEquals(
                "cls bootstrap --account 000000000000 --region us-east-2",
                BaseCDKCommand.bootstrapCommandFor(placement(null))
        );
    }

    @Test
    void withTempOutputDoesNotShareOutputPath() {
        CDKProcessExec processExec = new CDKProcessExec(() -> false, () -> 0, () -> "default");
        // apply the option defaults, as when mixed into a command
        new CommandLine(processExec).parseArgs();

        CDKProcessExec copy = processExec.withTempOutput();

        Assertions.assertEquals("cdk.out", processExec.getOutputPath());
        Assertions.assertNotEquals(processExec.getOutputPath(), copy.getOutputPath());
        Assertions.assertTrue(Files.isDirectory(Path.of(copy.getOutputPath())), copy.getOutputPath());
        Assertions.assertEquals(processExec.cdk(), copy.cdk());
        Assertions.assertEquals(processExec.cdkApp(), copy.cdkApp());
        Assertions.assertEquals(processExec.profile(), copy.profile());
    }

    Path project() throws IOException {
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

        return project;
    }

    static BaseCDKCommand commandWith(S3Client client) {
        return new BaseCDKCommand() {
            @Override
            protected S3 createS3(String awsProfile) {
                return new S3() {
                    @Override
                    protected S3Client createClient(String region) {
                        return client;
                    }
                };
            }
        };
    }

    static S3Exception headBucketFailure(S3Exception.Builder builder, int statusCode, String errorCode) {
        return (S3Exception) builder
                .statusCode(statusCode)
                .message(errorCode)
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode(errorCode)
                        .errorMessage(errorCode)
                        .build())
                .build();
    }

    @Test
    void existingBucketIsNotMissing() throws IOException {
        S3Client client = mock(S3Client.class);
        when(client.headBucket(any(HeadBucketRequest.class))).thenReturn((HeadBucketResponse) HeadBucketResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build());

        Assertions.assertEquals(List.of(), commandWith(client).findPlacementsMissingBootstrap(List.of(project().toFile()), "default", false));
    }

    @Test
    void missingBucketIsMissingBootstrap() throws IOException {
        S3Client client = mock(S3Client.class);
        when(client.headBucket(any(HeadBucketRequest.class))).thenThrow(headBucketFailure(NoSuchBucketException.builder(), 404, "NoSuchBucket"));

        List<Placement> missing = commandWith(client).findPlacementsMissingBootstrap(List.of(project().toFile()), "default", false);

        Assertions.assertEquals(1, missing.size(), missing.toString());
        Assertions.assertEquals("000000000000", missing.get(0).account());
        Assertions.assertEquals("us-east-2", missing.get(0).region());
        Assertions.assertEquals("prod", missing.get(0).stage());
    }

    @Test
    void missingBucketFailsDeployWithBootstrapCommand() {
        S3Client client = mock(S3Client.class);
        when(client.headBucket(any(HeadBucketRequest.class))).thenThrow(headBucketFailure(NoSuchBucketException.builder(), 404, "NoSuchBucket"));

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class, () -> commandWith(client).confirmBootstrapForPlacements(List.of(project().toFile()), "default", false));

        Assertions.assertTrue(exception.getMessage().contains("cls bootstrap --account 000000000000 --region us-east-2 --stage prod"), exception.getMessage());
        Assertions.assertTrue(exception.getMessage().contains("--bootstrap"), exception.getMessage());
    }

    @Test
    void accessDeniedIsNotMissingBootstrap() {
        S3Client client = mock(S3Client.class);
        when(client.headBucket(any(HeadBucketRequest.class))).thenThrow(headBucketFailure(S3Exception.builder(), 403, "AccessDenied"));

        // a deploy --bootstrap must not attempt a bootstrap, nor report "must bootstrap", when the check itself failed
        ExitCodeException exception = Assertions.assertThrows(ExitCodeException.class, () -> commandWith(client).findPlacementsMissingBootstrap(List.of(project().toFile()), "default", false));

        Assertions.assertTrue(exception.getMessage().contains("prod-clusterless-metadata-000000000000-us-east-2"), exception.getMessage());
        Assertions.assertTrue(exception.getMessage().contains("AccessDenied"), exception.getMessage());
        Assertions.assertFalse(exception.getMessage().contains("must bootstrap"), exception.getMessage());
    }

    @Test
    void dryRunAcceptsBootstrapAndOnlyPlansTheDeploy() throws IOException {
        Path project = project();

        int exitCode = new Kernel().execute(new String[]{"deploy", "--bootstrap", "--dry-run", "--output-path", tempDir.resolve("cdk.out").toString(), "-p", project.toString()});

        String out = systemOut.getText();

        // a dry run makes no AWS calls, so no placement is found missing and nothing is bootstrapped
        Assertions.assertEquals(0, exitCode, systemErr.getText());
        Assertions.assertTrue(out.contains(" deploy --all"), out);
        Assertions.assertFalse(out.contains(" bootstrap "), out);
    }
}
