/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.report;

import clusterless.cls.command.CommandWrapper;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "arcs",
        description = "List all deployed arcs.",
        subcommands = {ArcsCommand.ArcStatusCommand.class}
)
public class ArcsCommand extends CommandWrapper<ArcsCommandOptions> {
    public ArcsCommand() {
        super(new ArcsCommandOptions());
    }

    @CommandLine.Command(
            name = "status",
            description = "Show the status of the current target."
    )
    public static class ArcStatusCommand implements Callable<Integer> {
        @CommandLine.ParentCommand
        ArcsCommand arcsCommand;

        @CommandLine.Mixin
        ArcStatusCommandOption arcStatusCommandOption = new ArcStatusCommandOption();

        @Override
        public Integer call() throws Exception {
            return arcsCommand.main().run(arcStatusCommandOption);
        }
    }
}
