/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.bootstrap;

import clusterless.command.BootstrapCommandOptions;
import clusterless.substrate.aws.CommonCommand;
import clusterless.substrate.aws.ProcessExec;
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.substrate.aws.resources.Stacks;
import clusterless.util.Label;
import clusterless.util.Lists;
import clusterless.util.OrderedSafeMaps;
import clusterless.util.Strings;
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
public class Bootstrap extends CommonCommand implements Callable<Integer> {
    @CommandLine.Mixin
    ProcessExec processExec = new ProcessExec();

    @CommandLine.Mixin
    BootstrapCommandOptions commandOptions = new BootstrapCommandOptions();

    @Override
    public Integer call() throws Exception {
        if (commandOptions.synth()) {
            return synth();
        }
        return exec();
    }

    private Integer exec() {
        String account = prompt(commandOptions.account(), "Enter AWS account id to bootstrap: ");
        String region = prompt(commandOptions.region(), "Enter region to bootstrap: ");
        String stage = prompt(commandOptions.stage(), "Enter stage to bootstrap (hit enter for none): ");

        List<String> args = Lists.list(OrderedSafeMaps.of(
                "--account",
                account,
                "--region",
                region,
                "--stage",
                stage
        ));

        if (System.getenv("CLS_SYNTH_ONLY") != null) {
            synth();
            return 0;
        }

        List<String> kernelArgs = Lists.concat(List.of("--synth"), args);

        processExec.setUseTempOutput(true);
        processExec.executeCDKApp(getConfig(), "deploy", getRequireDeployApproval(), "bootstrap", kernelArgs);

        return 0;
    }

    private Integer synth() {

        Label stage = Label.of(commandOptions.stage());

        AppProps props = AppProps.builder()
                .build();

        StagedApp app = new StagedApp(props, stage);

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

        return 0;
    }

    protected String prompt(String value, String prompt) {
        if (value == null && System.console() != null) {
            return Strings.emptyToNull(System.console().readLine(prompt));
        }

        return value;
    }
}
