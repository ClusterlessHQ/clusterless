/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;

import clusterless.command.LifecycleCommandOptions;
import clusterless.substrate.aws.CommonCommand;
import clusterless.substrate.aws.cdk.lifecycle.Lifecycle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "synth",
        hidden = true
)
public class Synth extends CommonCommand implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Synth.class);
    @CommandLine.Mixin
    LifecycleCommandOptions commandOptions = new LifecycleCommandOptions();
    Lifecycle lifecycle = new Lifecycle();

    @Override
    public Integer call() throws IOException {
        LOG.info("exec synth using: {}", commandOptions.projectFiles());

        lifecycle.setConfigurations(kernel.configurations());

        lifecycle.synthProject(commandOptions.projectFiles());

        return 0;
    }
}
