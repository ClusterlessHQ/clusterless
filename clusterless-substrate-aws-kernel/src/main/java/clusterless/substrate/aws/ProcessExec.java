/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.command.LifecycleCommandOptions;
import clusterless.startup.Startup;
import clusterless.util.Lists;
import clusterless.util.OrderedSafeMaps;
import clusterless.util.URIUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
public class ProcessExec {
    private static final Logger LOG = LogManager.getLogger(ProcessExec.class);

    @CommandLine.Option(names = "--dry-run", description = "do not execute underlying cdk binary")
    public boolean dryRun = false;

    @CommandLine.Option(names = "--cdk", description = "path to the cdk binary")
    public String cdk = "cdk";

    @CommandLine.Option(names = "--cdk-app", description = "path to the cdk json file")
    public String cdkApp = URIUtil.normalize("%s/bin/cls-aws".formatted(System.getProperty(Startup.CLUSTERLESS_HOME)));

    @CommandLine.Option(names = "--profile", description = "aws profile")
    public String profile = System.getenv("AWS_PROFILE");

    @CommandLine.Option(names = "--output", description = "cloud assembly output directory")
    public String output = "cdk.out";

    public ProcessExec() {
    }

    public Integer executeLifecycleProcess(String command, LifecycleCommandOptions commandOptions) {
        List<String> cdkCommand = new LinkedList<>();

        cdkCommand.add(
                cdk
        );

        // execute the aws-cli app with the synth command
        String filesArg = filesAsArg(commandOptions.projectFiles());
        String appCommand = "%s synth --project %s".formatted(cdkApp, filesArg);

        // options only added if value is not null
        cdkCommand.addAll(
                Lists.list(OrderedSafeMaps.of(
                        "--app",
                        appCommand,
                        "--profile",
                        profile,
                        "--output",
                        output
                ))
        );

        cdkCommand.add(command);

        return executeProcess(cdkCommand);
    }

    protected Integer executeCDK(String... cdkArgs) {
        return executeProcess(Lists.asList(cdk, cdkArgs));
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
        LOG.info("command: {}", args);

        if (dryRun) {
            LOG.info("dry run, not executing command");
            return 0;
        }

        Process process = new ProcessBuilder(args)
                .inheritIO()
                .start();

        return process.waitFor();
    }

    private String filesAsArg(List<File> files) {
        return files.stream().map(Object::toString).collect(Collectors.joining(","));
    }
}
