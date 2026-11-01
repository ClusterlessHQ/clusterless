/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.bootstrap;

import clusterless.cls.command.BootstrapCommandOptions;
import clusterless.cls.config.CommonConfig;
import clusterless.cls.substrate.aws.AwsConfig;
import clusterless.cls.substrate.aws.cdk.BaseCDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKProcessExec;
import clusterless.cls.substrate.aws.meta.Metadata;
import clusterless.cls.substrate.aws.resources.Stacks;
import clusterless.cls.util.ExitCodeException;
import clusterless.cls.util.Lists;
import clusterless.commons.collection.OrderedSafeMaps;
import clusterless.commons.naming.Stage;
import clusterless.commons.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

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
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);
    @CommandLine.Mixin
    BootstrapCommandOptions commandOptions = new BootstrapCommandOptions();
    @CommandLine.Mixin
    CDKProcessExec processExec = new CDKProcessExec(commandOptions::dryRun, commandOptions::retry, this::verbosityLevel, commandOptions::profile);

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

        if (System.getenv("CLS_SYNTH_ONLY") != null) {
            synth();
            return 0;
        }

        processExec.setUseTempOutput(true);

        if (!destroyBootstrap) {
            List<String> approvals = getRequireDeployApproval(commandOptions.approve().orElse(null));
            return deployBootstrap(processExec, getCommonConfig(), getProviderConfig(), approvals, account, region, stage, commandOptions.dryRun());
        }

        List<String> kernelArgs = kernelArgs(account, region, stage);

        LOG.info("executing kernel with: {}", kernelArgs);

        List<String> approvals = getRequireDestroyApproval(commandOptions.approve().orElse(null));

        int exitCode = processExec.executeCDKApp(getCommonConfig(), getProviderConfig(), CDKCommand.DESTROY, approvals, "bootstrap", kernelArgs);

        if (exitCode != 0) {
            // logging is off at the default verbosity, so point at -v rather than at the logs
            String message = "unable to destroy bootstrap clusterless, rerun with -v for details";
            LOG.error(message);
            throw new ExitCodeException(message, exitCode);
        }

        return 0;
    }

    /**
     * Deploys the bootstrap stack for the given placement and pushes its metadata. Shared by {@code cls bootstrap}
     * and {@code cls deploy --bootstrap}.
     */
    public static int deployBootstrap(CDKProcessExec processExec, CommonConfig commonConfig, AwsConfig awsConfig, List<String> approvals, String account, String region, String stage, boolean dryRun) {
        List<String> kernelArgs = kernelArgs(account, region, stage);

        LOG.info("executing kernel with: {}", kernelArgs);

        int exitCode = processExec.executeCDKApp(commonConfig, awsConfig, CDKCommand.DEPLOY, approvals, "bootstrap", kernelArgs);

        if (exitCode != 0) {
            String message = String.format("unable to bootstrap clusterless, confirm the AWS cdk has been bootstrapped in the target account/region, try: cdk bootstrap aws://%s/%s", account, region);
            LOG.error(message);
            throw new ExitCodeException(message, exitCode);
        }

        return Metadata.pushBootstrapMetadata(processExec.profile(), region, processExec.getOutputPath(), dryRun);
    }

    private static List<String> kernelArgs(String account, String region, String stage) {
        List<String> args = Lists.list(OrderedSafeMaps.of(
                "--account",
                account,
                "--region",
                region,
                "--stage",
                Strings.emptyToNull(stage)
        ));

        return Lists.concat(List.of("--synth"), args);
    }

    private Integer synth() {

        Stage stage = Stage.of(commandOptions.stage());

        AppProps props = AppProps.builder()
                .build();

        BootstrapApp app = new BootstrapApp(props, stage);

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

        Metadata.writeBootstrapMetaLocal((BootstrapMeta) app.stagedMeta());

        return 0;
    }
}
