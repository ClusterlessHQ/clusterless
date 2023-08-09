/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk.lifecycle;

import clusterless.command.DeployCommandOptions;
import clusterless.substrate.aws.cdk.BaseCDKCommand;
import clusterless.substrate.aws.cdk.CDKCommand;
import clusterless.substrate.aws.cdk.CDKProcessExec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "deploy"
)
public class Deploy extends BaseCDKCommand implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Deploy.class);
    @CommandLine.Mixin
    DeployCommandOptions commandOptions = new DeployCommandOptions();
    @CommandLine.Mixin
    CDKProcessExec processExec = new CDKProcessExec(commandOptions);

    @Override
    public Integer call() throws Exception {

        confirmBootstrapForPlacements(commandOptions.projectFiles(), processExec.profile());

        return processExec.executeLifecycleProcess(
                getCommonConfig(),
                getProviderConfig(),
                commandOptions,
                CDKCommand.Deploy,
                getRequireDeployApproval(commandOptions.approve().orElse(null))
        );
    }

}
