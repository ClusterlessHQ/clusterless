/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.lifecycle;

import clusterless.cls.command.ProjectCommandOptions;
import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.substrate.aws.cdk.BaseCDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKCommand;
import clusterless.cls.substrate.aws.cdk.CDKProcessExec;
import clusterless.cls.substrate.aws.meta.Metadata;
import clusterless.cls.substrate.aws.util.TagsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "synth",
        hidden = true
)
public class Synth extends BaseCDKCommand implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Synth.class);
    @CommandLine.Mixin
    ProjectCommandOptions commandOptions = new ProjectCommandOptions();
    Lifecycle lifecycle = new Lifecycle();

    @Override
    public Integer call() throws IOException {
        LOG.info("exec synth using: {}", commandOptions.projectFiles());

        lifecycle.setConfigurations(kernel.configurations());

        List<Deployable> deployables = lifecycle.loadProjectModels(commandOptions.projectFiles());

        if (commandOptions.excludeAllArcs().orElse(false)) {
            LOG.info("exec synth without all arcs");
            for (Deployable deployable : deployables) {
                deployable.arcs().clear();
            }
        }

        for (String excludeArcName : commandOptions.excludeArcNames()) {
            LOG.info("exec synth without arc: {}", excludeArcName);
            for (Deployable deployable : deployables) {
                deployable.arcs().removeIf(arc -> arc.name().equals(excludeArcName));
            }
        }

        for (String onlyResourceName : commandOptions.onlyResourceNames()) {
            LOG.info("exec synth only resource: {}", onlyResourceName);
            for (Deployable deployable : deployables) {
                deployable.arcs().clear();
                deployable.boundaries().clear();
                deployable.barriers().clear();
                deployable.resources().removeIf(arc -> !arc.name().equals(onlyResourceName));
            }
        }

        if (commandOptions.excludeAllTags().orElse(false)) {
            TagsUtil.disable();
        }

        lifecycle.synthProjectModels(deployables);

        CDKCommand cdkCommand = CDKProcessExec.currentCommand();
        if (cdkCommand == CDKCommand.DEPLOY || cdkCommand == CDKCommand.DESTROY) {
            Metadata.writeProjectMetaLocal(deployables);
        }

        return 0;
    }
}
