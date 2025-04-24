/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.exec.ArcExecCommandOptions;
import clusterless.cls.managed.dataset.RemoteDatasetOwnerLookup;
import clusterless.cls.model.deploy.*;
import clusterless.cls.model.state.ArcState;
import clusterless.cls.printer.Printer;
import clusterless.cls.substrate.aws.event.ArcNotifyEvent;
import clusterless.cls.substrate.aws.io.ArcReader;
import clusterless.cls.substrate.aws.report.scanner.ArcScanner;
import clusterless.cls.substrate.aws.runtime.ArcDeployment;
import clusterless.cls.substrate.aws.runtime.ArcMeta;
import clusterless.cls.substrate.aws.sdk.StepFunction;
import clusterless.cls.substrate.aws.util.DatasetLookup;
import clusterless.cls.substrate.uri.ArcURI;
import clusterless.cls.substrate.uri.ManifestURI;
import clusterless.cls.util.IntervalUnitParser;
import clusterless.cls.util.Moment;
import clusterless.cls.util.Tuple2;
import clusterless.commons.collection.OrderedSafeMaps;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static clusterless.cls.substrate.aws.report.scanner.ArcScanner.scannerOrNull;

@CommandLine.Command(
        name = "exec"
)
public class ArcExec implements Callable<Integer> {
    @CommandLine.ParentCommand
    Arcs arcsCommand;
    @CommandLine.Mixin
    ArcExecCommandOptions arcExecCommandOptions = new ArcExecCommandOptions();

    @Override
    public Integer call() throws Exception {
        arcsCommand.commandOptions.setArcCommonOptions(arcExecCommandOptions.arcCommonOptions());

        Predicate<ArcRecord> arcRecordPredicate = arcRecord -> true;

        if (!arcExecCommandOptions.names().isEmpty()) {
            arcRecordPredicate = arcRecord -> arcExecCommandOptions.names().contains(arcRecord.name());
        }

        Supplier<Optional<Predicate<ArcState>>> arcStateSupplier = () -> {
            Predicate<ArcState> arcStatePredicate = null;

            if (!arcExecCommandOptions.states().isEmpty()) {
                arcStatePredicate = arcState -> arcExecCommandOptions.states().contains(arcState);
            }

            return Optional.ofNullable(arcStatePredicate);
        };

        String profile = arcsCommand.commandOptions.profile();
        Moment earliest = arcExecCommandOptions.earliest();
        Moment latest = arcExecCommandOptions.latest();
        List<String> lots = arcExecCommandOptions.lots();

        List<Tuple2<Moment, Moment>> ranges;
        if (!lots.isEmpty()) {
            ranges = toRanges(lots);
        } else if (earliest != null) {
            ranges = List.of(new Tuple2<>(earliest, latest));
        } else {
            throw new IllegalArgumentException("either a lot or range must be provided");
        }

        ArcReader arcReader = new ArcReader();
        StepFunction stepFunction = new StepFunction(profile);

        for (Tuple2<Moment, Moment> range : ranges) {
            @NotNull List<ArcScanner> arcScanners = scanViaRange(arcRecordPredicate, profile, range.get_1(), range.get_2(), arcStateSupplier);

            for (ArcScanner arcScanner : arcScanners) {
                ArcRecord record = arcScanner.record();

                // do the exec
                // must target the step function directly
                Placement placement = record.placement();
                Project project = record.project();
                String arcName = record.name();
                ArcURI arcURI = ArcURI.builder()
                        .withPlacement(placement)
                        .withProject(project)
                        .withArcName(arcName)
                        .build();

                ArcMeta arcMeta = arcReader.retrieve(arcURI.uri());

                List<String> sourceNames = arcExecCommandOptions.sources();

                Map<String, SourceDataset> arcSources = arcMeta.arc().sources();
                List<SourceDataset> datasets = new ArrayList<>();

                if (sourceNames.isEmpty()) {
                    datasets.addAll(arcSources.values());
                } else {
                    sourceNames.forEach(sourceName -> {
                        if (!arcSources.containsKey(sourceName)) {
                            throw new IllegalArgumentException("source " + sourceName + " not found in arc sources, available " + arcSources.keySet());
                        }
                        datasets.add(arcSources.get(sourceName));
                    });
                }

                RemoteDatasetOwnerLookup lookup = DatasetLookup.getRemoteDatasetOwnerLookup(arcExecCommandOptions.profile());

                for (SourceDataset dataset : datasets) {
                    // get the arc metadata
                    Optional<OwnedDataset> locatedDataset = lookup.lookup(placement, dataset);

                    ManifestURI manifestURI = ManifestURI.builder()
                            .withPlacement(placement)
                            .withDataset(dataset)
                            .build();

                    arcScanner.scan()
                            .forEach(arcStatusRecord -> {
                                SinkDataset sink = locatedDataset.orElseThrow().dataset();

                                Printer printer = arcsCommand.kernel().printer();
                                printer.print("starting arc exec for ");
                                printer.print("placement: " + arcStatusRecord.arcRecord().placement().display());
                                printer.print("project: " + arcStatusRecord.arcRecord().project().display());
                                printer.print("arc: " + arcStatusRecord.arcRecord().name());
                                printer.print("dataset: " + sink.display());
                                printer.print("state: " + arcStatusRecord.state());
                                printer.print("lot: " + arcStatusRecord.lotId());
                                printer.println();

                                ArcNotifyEvent notifyEvent = ArcNotifyEvent.builder()
                                        .withDataset(sink)
                                        .withLot(arcStatusRecord.lotId())
                                        .withManifest(manifestURI.uri())
                                        .build();

                                Map<String, Object> payload = OrderedSafeMaps.of("detail", notifyEvent);

                                ArcDeployment arcDeployment = arcMeta.arcDeployment();
                                String stepFunctionName = arcDeployment.stepFunctionName();

                                stepFunction.start(placement.account(), stepFunctionName, payload)
                                        .isSuccessOrThrow(e -> {
                                            throw new IllegalStateException("unable to start step function: " + stepFunctionName + ", " + e.getMessage(), e);
                                        });
                            });
                }
            }
        }

        return 0;
    }

    private static @NotNull List<Tuple2<Moment, Moment>> toRanges(List<String> lots) {
        return lots.stream()
                .map(IntervalUnitParser::convert)
                .map(l -> new Tuple2<>(l, l))
                .toList();
    }

    private @NotNull List<ArcScanner> scanViaRange(Predicate<ArcRecord> arcRecordPredicate, String profile, Moment earliest, Moment latest, Supplier<Optional<Predicate<ArcState>>> arcStateSupplier) {
        List<ArcScanner> list;
        try (Stream<ArcRecord> arcStream = arcsCommand.listAllArcs(arcRecordPredicate)) {
            // convert this to a list of lots within an arc
            list = arcStream
                    .map(arcRecord -> scannerOrNull(arcRecord, profile, earliest, latest, arcStateSupplier))
                    .filter(Objects::nonNull)
                    .toList();
        }
        return list;
    }
}
