/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.util.Lists;
import clusterless.util.OrderedSafeMaps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * <pre>
 *    -a, --app               REQUIRED WHEN RUNNING APP: command-line for executing
 *                            your app or a cloud assembly directory (e.g. "node
 *                            bin/my-app.js"). Can also be specified in context.json or
 *                            ~/.context.json                                  [string]
 *    --profile               Use the indicated AWS profile as the default
 *                            environment                                  [string]
 *    -o, --output            Emits the synthesized cloud assembly into a directory
 *                            (default: cdk.out)                           [string] </pre>
 */
public abstract class Manage {
    private static final Logger LOG = LogManager.getLogger(Manage.class);

    @CommandLine.ParentCommand
    Kernel kernel;

    public Manage() {
    }

    protected Integer executeLifecycleProcess(String command) {

        List<String> cdk = new LinkedList<>();
        cdk.add(
                kernel.cdk
        );

        String appCommand = "%s synth".formatted(kernel.cdkApp);

        cdk.addAll(
                Lists.list(OrderedSafeMaps.of(
                        "--app",
                        appCommand,
                        "--profile",
                        kernel.profile,
                        "--output",
                        kernel.output
                ))
        );

        cdk.add(command);

        return executeProcess(cdk);
    }

    protected Integer executeCDK(String... cdkArgs) {
        return executeProcess(Lists.asList(kernel.cdk, cdkArgs));
    }

    protected int executeProcess(String... args) {
        return executeProcess(List.of(args));
    }

    protected int executeProcess(List<String> args) {
        try {
            return process(args);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int process(List<String> args) throws IOException, InterruptedException {
        LOG.info("command args: {}", args);

        Process process = new ProcessBuilder(args)
                .inheritIO()
                .start();

        return process.waitFor();
    }
}
