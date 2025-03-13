/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.report.ArcStatusCommandOption;
import clusterless.cls.model.state.ArcState;
import clusterless.cls.substrate.aws.report.reporter.Reporter;
import clusterless.cls.substrate.aws.report.scanner.ArcScanner;
import clusterless.cls.util.Moment;
import picocli.CommandLine;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;
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

        Supplier<Optional<Predicate<ArcState>>> arcStateSupplier = () -> {
            Predicate<ArcState> arcStatePredicate = null;

            if (!arcStatusCommandOption.states().isEmpty()) {
                arcStatePredicate = arcState -> arcStatusCommandOption.states().contains(arcState);
            }

            return Optional.ofNullable(arcStatePredicate);
        };

        String profile = arcsCommand.commandOptions.profile();
        Moment earliest = arcStatusCommandOption.earliest();
        Moment latest = arcStatusCommandOption.latest();

        if (arcStatusCommandOption.list()) {
            Reporter<ArcStatusRecord> reporter = Reporter.instance(arcsCommand.kernel().printer(), ArcStatusRecord.class);

            try (Stream<ArcRecord> arcStream = arcsCommand.listAllArcs(arcRecordPredicate)) {
                reporter.report(arcStream
                        .map(arcRecord -> scannerOrNull(arcRecord, profile, earliest, latest, arcStateSupplier))
                        .filter(Objects::nonNull)
                        .flatMap(ArcScanner::scan)
                );
            }
        } else {
            Reporter<ArcStatusSummaryRecord> reporter = Reporter.instance(arcsCommand.kernel().printer(), ArcStatusSummaryRecord.class);

            try (Stream<ArcRecord> arcStream = arcsCommand.listAllArcs(arcRecordPredicate)) {
                reporter.report(arcStream
                        .map(arcRecord -> scannerOrNull(arcRecord, profile, earliest, latest, arcStateSupplier))
                        .filter(Objects::nonNull)
                        .map(ArcScanner::summarizeScan)
                );
            }
        }

        return 0;
    }

    private static ArcScanner scannerOrNull(ArcRecord arcRecord, String profile, Moment earliest, Moment latest, Supplier<Optional<Predicate<ArcState>>> arcStateSupplier) {
        try {
            return new ArcScanner(profile, arcRecord, earliest, latest, arcStateSupplier);
        } catch (IllegalStateException e) {
            return null;
        }
    }
}
