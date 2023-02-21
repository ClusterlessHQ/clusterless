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
import clusterless.util.URIs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
    private boolean dryRun = false;

    @CommandLine.Option(names = "--cdk", description = "path to the cdk binary")
    private String cdk = "cdk";

    @CommandLine.Option(names = "--cdk-app", description = "path to the cdk json file")
    private String cdkApp = URIs.normalize("%s/bin/cls-aws".formatted(System.getProperty(Startup.CLUSTERLESS_HOME)));

    @CommandLine.Option(names = "--profile", description = "aws profile")
    private String profile = System.getenv("AWS_PROFILE");

    @CommandLine.Option(names = "--output", description = "cloud assembly output directory")
    private String output = "cdk.out";

    @CommandLine.Option(
            names = "--use-temp-output",
            description = "place cloud assembly output into a temp directory",
            defaultValue = CommandLine.Option.NULL_VALUE,
            fallbackValue = "true",
            arity = "0..1",
            hidden = true
    )
    private Optional<Boolean> useTempOutput;

    public ProcessExec() {
    }

    public boolean dryRun() {
        return dryRun;
    }

    public String cdk() {
        return cdk;
    }

    public String cdkApp() {
        return cdkApp;
    }

    public String profile() {
        return profile;
    }

    public String output() {
        return output;
    }

    public Optional<Boolean> useTempOutput() {
        return useTempOutput;
    }

    public void setUseTempOutput(boolean useTempOutput) {
        if (this.useTempOutput.isEmpty()) {
            this.useTempOutput = Optional.of(useTempOutput);
        }
    }

    public Integer executeLifecycleProcess(String cdkCommand, LifecycleCommandOptions commandOptions) {
        String projectAgs = "--project %s".formatted(filesAsArg(commandOptions.projectFiles()));

        return executeCDKApp(cdkCommand, "synth", projectAgs);
    }

    public Integer executeCDKApp(String cdkCommand, String kernelCommand, String kernelArgs) {
        List<String> cdkCommands = new LinkedList<>();

        cdkCommands.add(
                cdk()
        );

        // execute the aws-cli app with the synth command
        String awsKernel = ("%s %s %s").formatted(cdkApp(), kernelCommand, kernelArgs);

        // options only added if value is not null
        cdkCommands.addAll(
                Lists.list(OrderedSafeMaps.of(
                        "--app",
                        awsKernel,
                        "--profile",
                        profile(),
                        "--output",
                        createOutputPath()
                ))
        );

        cdkCommands.addAll(
                List.of(
                        cdkCommand,
                        "--all" // deploy all stacks
                )
        );

        return executeProcess(cdkCommands);
    }

    private String createOutputPath() {
        if (useTempOutput().orElse(false)) {
            try {
                Path clusterless = Files.createTempDirectory("clusterless");

                LOG.info("placing cdk.out synth files in: {}", clusterless);

                return clusterless.toAbsolutePath().toString();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return output();
    }

    protected Integer executeCDK(String... cdkArgs) {
        return executeProcess(Lists.asList(cdk(), cdkArgs));
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

        if (dryRun()) {
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
