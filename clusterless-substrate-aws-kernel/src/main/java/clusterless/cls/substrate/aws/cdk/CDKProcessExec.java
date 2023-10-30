/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk;

import clusterless.cls.command.project.ProjectCommandOptions;
import clusterless.cls.config.CommonConfig;
import clusterless.cls.process.ProcessExec;
import clusterless.cls.startup.Startup;
import clusterless.cls.substrate.aws.AwsConfig;
import clusterless.cls.util.Lazy;
import clusterless.cls.util.Lists;
import clusterless.cls.util.URIs;
import clusterless.commons.collection.OrderedSafeMaps;
import clusterless.commons.collection.SafeList;
import com.google.common.base.Joiner;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
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
public class CDKProcessExec extends ProcessExec {
    private static final Logger LOG = LogManager.getLogger(CDKProcessExec.class);
    public static final String CLS_CDK_COMMAND = "CLS_CDK_COMMAND";
    public static final String CLS_CDK_OUTPUT_PATH = "CLS_CDK_OUTPUT_PATH";
    public static final String CLS_CDK_PROFILE = "CLS_CDK_PROFILE";

    @CommandLine.Option(names = "--cdk", description = {"path to the cdk binary", "uses $PATH by default to search for 'cdk'"})
    private String cdk = "cdk";

    @CommandLine.Option(names = "--cdk-app", description = "path to the cls-aws kernel")
    private String cdkApp = URIs.normalize(String.format("%s/bin/cls-aws", System.getProperty(Startup.CLUSTERLESS_HOME)));

    @CommandLine.Option(
            names = "--use-localstack",
            description = "use localstack at the given host:port, uses 'localhost' if not provided",
            arity = "0..1",
            defaultValue = CommandLine.Option.NULL_VALUE,
            fallbackValue = "localhost"
    )
    private Optional<String> useLocalStackHost;


    @CommandLine.Option(names = "--output-path", description = "cloud assembly output directory")
    private String output = "cdk.out";

    @CommandLine.Option(
            names = "--use-temp-output-path",
            description = "place cloud assembly output into a temp directory",
            defaultValue = CommandLine.Option.NULL_VALUE,
            fallbackValue = "true",
            arity = "0..1",
            hidden = true
    )
    private Optional<Boolean> useTempOutput;

    private final Lazy<String> outputPath = Lazy.of(this::createOutputPath);
    private Supplier<String> profile;

    public CDKProcessExec() {
    }

    public CDKProcessExec(Supplier<Boolean> dryRun, Supplier<Integer> verbosity, Supplier<String> profile) {
        super(dryRun, verbosity);
        this.profile = profile;
    }

    public CDKProcessExec(Supplier<Boolean> dryRun, Supplier<Boolean> retry, Supplier<Integer> verbosity, Supplier<String> profile) {
        super(dryRun, retry, verbosity);
        this.profile = profile;
    }

    public String cdk() {
        return cdk;
    }

    public String cdkApp() {
        return cdkApp;
    }

    public String profile() {
        return profile.get();
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

    private String getCKDBinary() {
        if (useLocalStackHost.isPresent()) {
            return "cdklocal";
        }

        return cdk();
    }

    public Integer executeLifecycleProcess(@NotNull CommonConfig commonConfig, @NotNull AwsConfig awsConfig, @NotNull ProjectCommandOptions commandOptions, @NotNull CDKCommand cdkCommand) {
        return executeLifecycleProcess(commonConfig, awsConfig, commandOptions, cdkCommand, Collections.emptyList());
    }

    public Integer executeLifecycleProcess(@NotNull CommonConfig commonConfig, @NotNull AwsConfig awsConfig, @NotNull ProjectCommandOptions commandOptions, @NotNull CDKCommand cdkCommand, @NotNull List<String> cdkCommandArgs) {

        List<String> kernelArgs = new LinkedList<>();

        kernelArgs.addAll(List.of("--project", filesAsArg(commandOptions.projectFiles())));
        kernelArgs.addAll(Lists.list(OrderedSafeMaps.of("--exclude-all-arcs", commandOptions.excludeAllArcs().map(b -> Boolean.toString(b)).orElse(null))));
        kernelArgs.addAll(Lists.list(OrderedSafeMaps.of("--exclude-arc", commandOptions.excludeArcNames().isEmpty() ? null : String.join(",", commandOptions.excludeArcNames()))));
        kernelArgs.addAll(Lists.list(OrderedSafeMaps.of("--only-resource", commandOptions.onlyResourceNames().isEmpty() ? null : String.join(",", commandOptions.onlyResourceNames()))));
        kernelArgs.addAll(Lists.list(OrderedSafeMaps.of("--exclude-all-tags", commandOptions.excludeAllTags().map(b -> Boolean.toString(b)).orElse(null))));
        kernelArgs.addAll(SafeList.of(verbosity.get() == 0 ? null : String.format("-%s", "v".repeat(verbosity.get()))));

        return executeCDKApp(commonConfig, awsConfig, cdkCommand, cdkCommandArgs, "synth", kernelArgs);
    }

    public Integer executeCDKApp(@NotNull CommonConfig commonConfig, @NotNull AwsConfig awsConfig, @NotNull CDKCommand cdkCommand, @NotNull List<String> commandArgs, @NotNull String kernelCommand, @NotNull List<String> kernelArgs) {
        List<String> cdkCommands = new LinkedList<>();

        cdkCommands.add(
                getCKDBinary()
        );

        List<String> appArgs = addPropertiesToArgs(commonConfig, awsConfig);

        // execute the aws-cli app with the synth command
        String awsKernel = Joiner.on(" ").join(
                cdkApp(),
                Joiner.on(" ").join(appArgs),
                kernelCommand,
                Joiner.on(" ").join(kernelArgs)
        );

        // options only added if value is not null
        cdkCommands.addAll(
                Lists.list(OrderedSafeMaps.of(
                        "--app",
                        awsKernel,
                        "--profile",
                        profile(),
                        "--output",
                        getOutputPath()
                ))
        );

        cdkCommands.addAll(
                SafeList.of(
                        cdkCommand.command(),
                        cdkCommand != CDKCommand.IMPORT ? "--all" : null // deploy all stacks
                )
        );

        cdkCommands.addAll(commandArgs);

        Map<String, String> environment = OrderedSafeMaps.of(
                CLS_CDK_COMMAND, cdkCommand.command(),
                CLS_CDK_OUTPUT_PATH, getOutputPath(),
                CLS_CDK_PROFILE, profile()
        );

        return executeProcess(environment, cdkCommands);
    }

    public String getOutputPath() {
        return outputPath.get();
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

    public Integer executeCDK(String... cdkArgs) {
        return executeProcess(Collections.emptyMap(), Lists.asList(cdk(), cdkArgs));
    }

    @Override
    protected Map<String, String> getCommonEnvironment() {
        return OrderedSafeMaps.of(
                "JSII_SILENCE_WARNING_DEPRECATED_NODE_VERSION", "true",
                "LOCALSTACK_HOSTNAME", getLocalStackHostName(),
                "EDGE_PORT", getLocalStackPort()
        );
    }

    protected String getLocalStackHostName() {
        return useLocalStackHost.flatMap(s -> Arrays.stream(s.split(":", 2)).findFirst()).orElse(null);
    }

    protected String getLocalStackPort() {
        return useLocalStackHost.flatMap(s -> Arrays.stream(s.split(":", 2)).skip(1).findFirst()).orElse(null);
    }

    protected String filesAsArg(List<File> files) {
        return files.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    public static CDKCommand currentCommand() {
        return CDKCommand.from(System.getenv().get(CLS_CDK_COMMAND));
    }

    public static Path cdkLocalOutputPath() {
        return Optional.ofNullable(System.getenv().get(CLS_CDK_OUTPUT_PATH)).map(Paths::get).orElse(null);
    }
}
