/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.report.ArcStatusCommandOption;
import clusterless.cls.substrate.aws.report.reporter.Reporter;
import clusterless.cls.substrate.aws.report.scanner.ArcScanner;
import clusterless.cls.util.Moment;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "status"
)
public class ArcStatus implements Callable<Integer> {
    @CommandLine.ParentCommand
    Arcs arcsCommand;

    @CommandLine.Mixin
    ArcStatusCommandOption arcStatusCommandOption = new ArcStatusCommandOption();

    @Override
    public Integer call() throws Exception {
        arcsCommand.commandOptions.setProfile(arcStatusCommandOption.profile());
        arcsCommand.commandOptions.setAccount(arcStatusCommandOption.account());
        arcsCommand.commandOptions.setRegion(arcStatusCommandOption.region());
        arcsCommand.commandOptions.setStage(arcStatusCommandOption.stage());
        arcsCommand.commandOptions.setProjects(arcStatusCommandOption.projects());

        Predicate<ArcRecord> arcRecordPredicate = arcRecord -> true;

        if (!arcStatusCommandOption.names().isEmpty()) {
            arcRecordPredicate = arcRecord -> arcStatusCommandOption.names().contains(arcRecord.name());
        }

        String profile = arcsCommand.commandOptions.profile();
        Moment earliest = arcStatusCommandOption.earliest();
        Moment latest = arcStatusCommandOption.latest();

        if (arcStatusCommandOption.list()) {
            Reporter<ArcStatusRecord> reporter = Reporter.instance(arcsCommand.kernel().printer(), ArcStatusRecord.class);

            try (Stream<ArcRecord> arcStream = arcsCommand.listAllArcs(arcRecordPredicate)) {
                reporter.report(arcStream
                        .map(arcRecord -> new ArcScanner(profile, arcRecord, earliest, latest))
                        .flatMap(ArcScanner::scan)
                );
            }
        } else {
            Reporter<ArcStatusSummaryRecord> reporter = Reporter.instance(arcsCommand.kernel().printer(), ArcStatusSummaryRecord.class);

            try (Stream<ArcRecord> arcStream = arcsCommand.listAllArcs(arcRecordPredicate)) {
                reporter.report(arcStream
                        .map(arcRecord -> new ArcScanner(profile, arcRecord, earliest, latest))
                        .map(ArcScanner::summarizeScan)
                );
            }
        }

        return 0;
    }
}
