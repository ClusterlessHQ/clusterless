/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.lifecycle;

import clusterless.cls.command.project.VerifyCommandOptions;
import clusterless.cls.substrate.aws.cdk.BaseCDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKProcessExec;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "verify"
)
public class Verify extends BaseCDKCommand implements Callable<Integer> {
    @CommandLine.Mixin
    VerifyCommandOptions commandOptions = new VerifyCommandOptions();
    @CommandLine.Mixin
    CDKProcessExec processExec = new CDKProcessExec(commandOptions::dryRun, this::verbosityLevel, commandOptions::profile);

    public Verify() {
    }

    @Override
    public Integer call() {
        return processExec.executeLifecycleProcess(getCommonConfig(), getProviderConfig(), commandOptions, CDKCommand.SYNTH);
    }
}
