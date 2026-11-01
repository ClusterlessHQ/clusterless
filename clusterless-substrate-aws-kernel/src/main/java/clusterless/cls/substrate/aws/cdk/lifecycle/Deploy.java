/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.lifecycle;

import clusterless.cls.command.project.DeployCommandOptions;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.substrate.aws.cdk.BaseCDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKProcessExec;
import clusterless.cls.substrate.aws.cdk.bootstrap.Bootstrap;
import clusterless.cls.substrate.aws.meta.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "deploy"
)
public class Deploy extends BaseCDKCommand implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(Deploy.class);
    @CommandLine.Mixin
    DeployCommandOptions commandOptions = new DeployCommandOptions();
    @CommandLine.Mixin
    CDKProcessExec processExec = new CDKProcessExec(commandOptions::dryRun, commandOptions::retry, this::verbosityLevel, commandOptions::profile);

    @Override
    public Integer call() throws Exception {

        if (commandOptions.bootstrap()) {
            bootstrapMissingPlacements();
        }

        confirmBootstrapForPlacements(commandOptions.projectFiles(), processExec.profile(), commandOptions.dryRun());

        Integer exitCode = processExec.executeLifecycleProcess(
                getCommonConfig(),
                getProviderConfig(),
                commandOptions,
                CDKCommand.DEPLOY,
                getRequireDeployApproval(commandOptions.approve().orElse(null))
        );

        if (exitCode != 0) {
            return exitCode;
        }

        return Metadata.pushDeployablesMetadata(processExec.profile(), processExec.getOutputPath(), commandOptions.dryRun());
    }

    private void bootstrapMissingPlacements() throws IOException {
        List<Placement> missing = findPlacementsMissingBootstrap(commandOptions.projectFiles(), processExec.profile(), commandOptions.dryRun());

        List<String> approvals = getRequireDeployApproval(commandOptions.approve().orElse(null));

        for (Placement placement : missing) {
            LOG.info("bootstrapping placement: {}", placement);

            // each bootstrap gets its own temp output path so the deploy cloud assembly is not overwritten,
            // failures throw naming the account and region
            Bootstrap.deployBootstrap(processExec.withTempOutput(), getCommonConfig(), getProviderConfig(), approvals, placement.account(), placement.region(), placement.stage(), commandOptions.dryRun());
        }
    }

}
