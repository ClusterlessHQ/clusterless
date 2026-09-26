/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws;

import clusterless.cls.util.ExecutionExceptionHandler;
import clusterless.cls.util.ExitCodeExceptionMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.stream.SystemErr;
import uk.org.webcompere.systemstubs.stream.SystemOut;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Logging is off at the default verbosity, so a dry run must print its plan as user output
 * (stdout, via picocli's out stream) rather than only as a log line.
 */
@ExtendWith(SystemStubsExtension.class)
public class DryRunOutputTest {
    @SystemStub
    SystemOut systemOut;

    @SystemStub
    SystemErr systemErr;

    @TempDir
    Path tempDir;

    Path project;

    @BeforeEach
    void setUp() throws IOException {
        project = tempDir.resolve("project.json");

        Files.writeString(project, """
                {
                  "project": { "name": "TestProject", "version": "20230101-00" },
                  "placement": { "stage": "prod", "provider": "aws", "account": "000000000000", "region": "us-east-2" },
                  "resources": [
                    { "type": "aws:core:s3Bucket", "name": "bucket", "bucketName": "sample-bucket" }
                  ]
                }
                """);
    }

    private String[] deployDryRun() {
        return new String[]{"deploy", "--dry-run", "--output-path", tempDir.resolve("cdk.out").toString(), "-p", project.toString()};
    }

    @Test
    void dryRunPrintsThePlanOnStdoutAtDefaultVerbosity() {
        int exitCode = new Kernel().execute(deployDryRun());

        String out = systemOut.getText();

        Assertions.assertEquals(0, exitCode, systemErr.getText());
        Assertions.assertTrue(out.contains("dry run: cdk "), out);
        Assertions.assertTrue(out.contains(" deploy --all"), out);
    }

    @Test
    void dryRunPrintsThroughTheCommandLineOutStream() {
        StringWriter out = new StringWriter();
        Kernel kernel = new Kernel();

        int exitCode = new CommandLine(kernel)
                .setExitCodeExceptionMapper(new ExitCodeExceptionMapper())
                .setExecutionExceptionHandler(new ExecutionExceptionHandler(kernel))
                .setOut(new PrintWriter(out, true))
                .execute(deployDryRun());

        Assertions.assertEquals(0, exitCode, systemErr.getText());
        Assertions.assertTrue(out.toString().contains("dry run: cdk "), out.toString());
        Assertions.assertFalse(systemOut.getText().contains("dry run:"), systemOut.getText());
    }

    @Test
    void executedCommandDoesNotPrintADryRunPlan() {
        new Kernel().execute(new String[]{"info", "which"});

        Assertions.assertFalse(systemOut.getText().contains("dry run:"), systemOut.getText());
    }
}
