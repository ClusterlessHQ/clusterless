/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.bootstrap;

import clusterless.cls.command.BootstrapCommandOptions;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger LOG = LogManager.getLogger(Bootstrap.class);
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

        CDKCommand cdkCommand = destroyBootstrap ? CDKCommand.DESTROY : CDKCommand.DEPLOY;
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

        return Metadata.pushBootstrapMetadata(commandOptions.profile(), region, processExec.getOutputPath(), commandOptions.dryRun());
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
