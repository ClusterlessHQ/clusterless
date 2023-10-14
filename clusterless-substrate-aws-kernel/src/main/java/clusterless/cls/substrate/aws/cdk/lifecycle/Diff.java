/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.lifecycle;

import clusterless.cls.command.project.DiffCommandOptions;
import clusterless.cls.substrate.aws.cdk.BaseCDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKProcessExec;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "diff"
)
public class Diff extends BaseCDKCommand implements Callable<Integer> {
    @CommandLine.Mixin
    DiffCommandOptions commandOptions = new DiffCommandOptions();
    @CommandLine.Mixin
    CDKProcessExec processExec = new CDKProcessExec(commandOptions::dryRun, this::verbosityLevel, commandOptions::profile);

    @Override
    public Integer call() throws Exception {
        return processExec.executeLifecycleProcess(getCommonConfig(), getProviderConfig(), commandOptions, CDKCommand.DIFF);
    }
}
