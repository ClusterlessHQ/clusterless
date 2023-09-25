/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.report;

import clusterless.command.report.PlacementsCommandOptions;
import clusterless.model.deploy.Placement;
import clusterless.substrate.aws.report.reporter.Reporter;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 */
@CommandLine.Command(
        name = "placements"
)
public class Placements extends Reports implements Callable<Integer> {
    @CommandLine.Mixin
    PlacementsCommandOptions commandOptions = new PlacementsCommandOptions();

    @Override
    public Integer call() throws Exception {
        List<Placement> placements = filterPlacements(commandOptions);

        Reporter<Placement> reporter = Reporter.instance(kernel().printer(), Placement.class);

        reporter.report(placements);

        return 0;
    }
}
