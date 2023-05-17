/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk.lifecycle;

import clusterless.command.ProjectCommandOptions;
import clusterless.model.deploy.Deployable;
import clusterless.substrate.aws.cdk.CDKCommand;
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
public class Synth extends CDKCommand implements Callable<Integer> {
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

        lifecycle.synthProjectModels(deployables);

        return 0;
    }
}
