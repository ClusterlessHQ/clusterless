/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk.lifecycle;


import clusterless.command.DestroyCommandOptions;
import clusterless.substrate.aws.cdk.BaseCDKCommand;
import clusterless.substrate.aws.cdk.CDKCommand;
import clusterless.substrate.aws.cdk.CDKProcessExec;
import clusterless.substrate.aws.meta.Metadata;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "destroy"
)
public class Destroy extends BaseCDKCommand implements Callable<Integer> {
    @CommandLine.Mixin
    DestroyCommandOptions commandOptions = new DestroyCommandOptions();
    @CommandLine.Mixin
    CDKProcessExec processExec = new CDKProcessExec(commandOptions::dryRun, commandOptions::retry, this::verbosityLevel);

    @Override
    public Integer call() throws Exception {
        Integer exitCode = processExec.executeLifecycleProcess(
                getCommonConfig(),
                getProviderConfig(),
                commandOptions,
                CDKCommand.DESTROY,
                getRequireDestroyApproval(commandOptions.approve().orElse(null))
        );

        if (exitCode != 0) {
            return exitCode;
        }

        return Metadata.removeDeployablesMetadata(processExec.getOutputPath(), commandOptions.dryRun());
    }
}
