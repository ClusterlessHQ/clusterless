/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.lifecycle;

import clusterless.cls.command.project.ImportCommandOptions;
import clusterless.cls.substrate.aws.cdk.BaseCDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKProcessExec;
import clusterless.cls.util.SafeList;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "import"
)
public class Import extends BaseCDKCommand implements Callable<Integer> {
    @CommandLine.Mixin
    ImportCommandOptions commandOptions = new ImportCommandOptions();
    @CommandLine.Mixin
    CDKProcessExec processExec = new CDKProcessExec(commandOptions::dryRun, this::verbosityLevel, commandOptions::profile);

    @Override
    public Integer call() throws Exception {

        confirmBootstrapForPlacements(commandOptions.projectFiles(), processExec.profile(), commandOptions.dryRun());

        List<String> args = SafeList.of(
                "ResourceBoundary",
                "--force"
        );

        args.addAll(getRequireImportApproval(commandOptions.approve().orElse(null)));

        // cdk import does not support tags
        commandOptions.setExcludeAllTags(true);

        return processExec.executeLifecycleProcess(
                getCommonConfig(),
                getProviderConfig(),
                commandOptions,
                CDKCommand.IMPORT,
                args
        );
    }

    private List<String> getRequireImportApproval(Boolean approve) {
        if ((approve != null && approve)) {
            return List.of(
                    // documented here but non functional
                    // https://github.com/aws/aws-cdk-rfcs/blob/master/text/0052-resource-importing-support.md
                    "--non-interactive"
            );
        }

        return Collections.emptyList();
    }
}
