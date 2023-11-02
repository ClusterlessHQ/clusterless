/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.report.PlacementsCommandOptions;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.substrate.aws.report.reporter.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger LOG = LoggerFactory.getLogger(Placements.class);
    @CommandLine.Mixin
    PlacementsCommandOptions commandOptions = new PlacementsCommandOptions();

    @Override
    public Integer call() throws Exception {
        LOG.info("using profile: {}", commandOptions.profile());

        List<Placement> placements = filterPlacements(commandOptions);

        Reporter<Placement> reporter = Reporter.instance(kernel().printer(), Placement.class);

        reporter.report(placements);

        return 0;
    }
}
