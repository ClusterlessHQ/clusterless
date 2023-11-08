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
        name = "datasets",
        description = "List all deployed datasets.",
        subcommands = {DatasetsCommand.DatasetStatusCommand.class}
)
public class DatasetsCommand extends CommandWrapper<DatasetsCommandOptions> {
    public DatasetsCommand() {
        super(new DatasetsCommandOptions());
    }

    @CommandLine.Command(
            name = "status",
            description = "Show the status of the current target."
    )
    public static class DatasetStatusCommand implements Callable<Integer> {
        @CommandLine.ParentCommand
        DatasetsCommand datasetsCommand;

        @CommandLine.Mixin
        DatasetStatusCommandOption datasetStatusCommandOption = new DatasetStatusCommandOption();

        @Override
        public Integer call() throws Exception {
            return datasetsCommand.main().run(datasetStatusCommandOption);
        }
    }
}
