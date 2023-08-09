/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk.bootstrap;

import clusterless.command.BootstrapCommandOptions;
import clusterless.json.JSONUtil;
import clusterless.naming.Stage;
import clusterless.substrate.aws.cdk.BaseCDKCommand;
import clusterless.substrate.aws.cdk.CDKCommand;
import clusterless.substrate.aws.cdk.CDKProcessExec;
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.substrate.aws.resources.Stacks;
import clusterless.substrate.aws.sdk.S3;
import clusterless.util.ExitCodeException;
import clusterless.util.Lists;
import clusterless.util.OrderedSafeMaps;
import clusterless.util.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * cdk assets bucket:
 * cdk-hnb659fds-assets-086903124729-us-west-2
 * Bucket Versioning
 * Enabled
 * <p>
 * ecr assets bucket
 * cdk-hnb659fds-container-assets-086903124729-us-west-2
 */
@CommandLine.Command(
        name = "bootstrap"
)
public class Bootstrap extends BaseCDKCommand implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Bootstrap.class);
    @CommandLine.Mixin
    BootstrapCommandOptions commandOptions = new BootstrapCommandOptions();
    @CommandLine.Mixin
    CDKProcessExec processExec = new CDKProcessExec(commandOptions::dryRun, commandOptions::retry);

    @Override
    public Integer call() throws Exception {
        if (commandOptions.synth()) {
            return synth();
        }
        return exec();
    }

    private Integer exec() {
        boolean destroyBootstrap = commandOptions.destroy();
        String account = prompt(commandOptions.account(), "Enter AWS account id to bootstrap: ");
        String region = prompt(commandOptions.region(), "Enter region to bootstrap: ");
        String stage = prompt(commandOptions.stage(), "Enter stage to bootstrap (hit enter for none): ");

        List<String> args = Lists.list(OrderedSafeMaps.of(
                "--account",
                account,
                "--region",
                region,
                "--stage",
                Strings.emptyToNull(stage)
        ));

        if (System.getenv("CLS_SYNTH_ONLY") != null) {
            synth();
            return 0;
        }

        List<String> kernelArgs = Lists.concat(List.of("--synth"), args);

        LOG.info("executing kernel with: {}", kernelArgs);

        processExec.setUseTempOutput(true);

        CDKCommand cdkCommand = destroyBootstrap ? CDKCommand.Destroy : CDKCommand.Deploy;
        List<String> approvals = destroyBootstrap ? getRequireDestroyApproval(commandOptions.approve().orElse(null)) : getRequireDeployApproval(commandOptions.approve().orElse(null));

        int exitCode = processExec.executeCDKApp(getCommonConfig(), getProviderConfig(), cdkCommand, approvals, "bootstrap", kernelArgs);

        if (!destroyBootstrap && exitCode != 0) {
            String message = String.format("unable to bootstrap clusterless, confirm the AWS cdk has been bootstrapped in the target account/region, try: cdk bootstrap aws://%s/%s", account, region);
            LOG.error(message);
            throw new ExitCodeException(message, exitCode);
        }

        if (destroyBootstrap && exitCode != 0) {
            LOG.error("unable to destroy bootstrap clusterless, see logs for details");
            throw new ExitCodeException("unable to destroy bootstrap clusterless, see logs for details", exitCode);
        }

        if (destroyBootstrap) {
            return 0;
        }

        Path bootstrapMetaPath = createBootstrapMetaPath(processExec.getOutputPath());

        LOG.info("reading metadata from: {}", bootstrapMetaPath.toAbsolutePath());

        if (commandOptions.dryRun()) {
            LOG.info("dry run, skipping metadata upload");
            return 0;
        }

        BootstrapMeta bootstrapMeta = JSONUtil.readAsObjectSafe(bootstrapMetaPath, BootstrapMeta.class);

        S3 s3 = new S3(processExec.profile(), region);

        URI metaURI = S3.createS3URI(bootstrapMeta.exports().get("metadata").name(), "metadata.json");

        LOG.info("putting metadata in: {}", metaURI);

        s3.put(metaURI, "application/json", bootstrapMeta)
                .isSuccessOrThrowRuntime(r -> String.format("unable to upload bootstrap metadata to: %s, %s", metaURI, r.errorMessage()));

        return 0;
    }

    private Integer synth() {

        Stage stage = Stage.of(commandOptions.stage());

        AppProps props = AppProps.builder()
                .build();

        StagedApp app = new StagedApp(props, stage);

        BootstrapMeta deployMeta = new BootstrapMeta();
        app.setDeployMeta(deployMeta);

        String stackName = Stacks.bootstrapStackName(stage);

        StackProps stackProps = StackProps.builder()
                .stackName(stackName)
                .description("This stack includes resources needed to manage Clusterless projects in this environment")
                .env(Environment.builder()
                        .account(commandOptions.account())
                        .region(commandOptions.region())
                        .build())
                .build();

        new BootstrapStack(app, stackProps);

        app.synth();

        writeBootstrapMeta(deployMeta);

        return 0;
    }

    private static void writeBootstrapMeta(BootstrapMeta bootstrapMeta) {
        String outputPath = System.getenv().get(CDKProcessExec.CLS_CDK_OUTPUT_PATH);

        if (outputPath != null) {
            Path bootstrapMetaPath = createBootstrapMetaPath(outputPath);
            LOG.info("writing metadata to: {}", bootstrapMetaPath.toAbsolutePath());

            try {
                Files.createDirectories(bootstrapMetaPath.getParent());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            JSONUtil.writeAsStringSafe(bootstrapMetaPath, bootstrapMeta);
        }
    }

    @NotNull
    private static Path createBootstrapMetaPath(String outputPath) {
        return Paths.get(outputPath).resolve("bootstrap").resolve("meta.json");
    }

    protected String prompt(String value, String prompt) {
        if (value == null && System.console() != null) {
            return Strings.emptyToNull(System.console().readLine(prompt));
        }

        return value;
    }
}
