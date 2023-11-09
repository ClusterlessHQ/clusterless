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
            description = {
                    "Show a summary of deployed arcs status (within the time range).",
                    "Where status is one of:",
                    "",
                    "- running: workload is in process",
                    "- complete: workload finished successfully",
                    "- partial: workload finished but some data is missing",
                    "- missing: no data, possibly due to error, or missing upstream data",
                    "",
                    "The --list option will output all arc instances instead of summarizing."
            }
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
