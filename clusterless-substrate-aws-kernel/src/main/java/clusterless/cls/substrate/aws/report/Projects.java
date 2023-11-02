/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.report.ProjectsCommandOptions;
import clusterless.cls.substrate.aws.report.reporter.Reporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.stream.Stream;

/**
 *
 */
@CommandLine.Command(
        name = "projects"
)
public class Projects extends Reports implements Callable<Integer> {
    private static final Logger LOG = LoggerFactory.getLogger(Projects.class);

    @CommandLine.Mixin
    ProjectsCommandOptions commandOptions = new ProjectsCommandOptions();

    @Override
    public Integer call() throws Exception {
        LOG.info("using profile: {}", commandOptions.profile());

        Stream<ProjectRecord> records = listAllProjects(commandOptions);

        Reporter<ProjectRecord> reporter = Reporter.instance(kernel().printer(), ProjectRecord.class);

        reporter.report(records);

        return 0;
    }
}
