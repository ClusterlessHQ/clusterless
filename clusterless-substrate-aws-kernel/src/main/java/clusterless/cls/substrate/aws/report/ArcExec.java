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
import clusterless.cls.substrate.aws.report.scanner.ArcStatusScanner;
import clusterless.cls.substrate.aws.report.scanner.ManifestScanner;
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
import clusterless.commons.util.Strings;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static clusterless.cls.substrate.aws.report.scanner.ArcStatusScanner.scannerOrNull;

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
        Printer printer = arcsCommand.kernel().printer();

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

        // if lots are provided, it is probably missing and it should be executed
        // should be an option with the range
        boolean fillGaps = !lots.isEmpty();

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
            Moment currentEarliest = range.get_1();
            Moment currentLatest = range.get_2();
            printer.println("handling range: " + currentEarliest + ":" + currentLatest);

            @NotNull List<ArcStatusScanner> arcStatusScanners = scanViaRange(arcRecordPredicate, profile, currentEarliest, currentLatest, arcStateSupplier, fillGaps);

            if (arcStatusScanners.isEmpty()) {
                printer.println("no arcs found in range " + currentEarliest + ":" + currentLatest + " for projects " + arcExecCommandOptions.projects() + " and arcs " + arcExecCommandOptions.names());
                continue;
            }

            for (ArcStatusScanner arcStatusScanner : arcStatusScanners) {
                handleScanner(printer, arcStatusScanner, arcReader, stepFunction);
            }
        }

        return 0;
    }

    private void handleScanner(Printer printer, ArcStatusScanner arcStatusScanner, ArcReader arcReader, StepFunction stepFunction) {
        ArcRecord record = arcStatusScanner.record();

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

        List<SourceDataset> datasets = findSourceDatasetsFor(arcMeta, sourceNames);

        RemoteDatasetOwnerLookup datasetOwnerLookup = DatasetLookup.getRemoteDatasetOwnerLookup(arcExecCommandOptions.profile());

        for (SourceDataset sourceDataset : datasets) {
            Optional<OwnedDataset> locatedDataset = datasetOwnerLookup.lookup(placement, sourceDataset);

            SinkDataset sink = locatedDataset.orElseThrow().dataset();

            Stream<ArcStatusRecord> scanStream = arcStatusScanner.scan();

            scanStream
                    .forEach(arcStatusRecord -> {
                        Moment moment = IntervalUnitParser.convert(arcStatusRecord.lotId);
                        ManifestScanner manifestScanner = new ManifestScanner(arcStatusScanner.profile(), new DatasetRecord(record.placement, sourceDataset), moment, moment);

                        manifestScanner.scan().forEach(statusRecord -> execStepFunction(printer, stepFunction, arcStatusRecord, sink, statusRecord.uri(), arcMeta, placement));
                    });
        }
    }

    private static void execStepFunction(Printer printer, StepFunction stepFunction, ArcStatusRecord arcStatusRecord, SinkDataset sink, ManifestURI manifestURI, ArcMeta arcMeta, Placement placement) {
        printer.print("starting arc exec for");
        printer.print(" placement: " + arcStatusRecord.arcRecord().placement().display());
        printer.print(" project: " + arcStatusRecord.arcRecord().project().display());
        printer.print(" arc: " + arcStatusRecord.arcRecord().name());
        printer.print(" dataset: " + sink.display());
        printer.print(" state: " + Strings.nullToEmpty(arcStatusRecord.state()));
        printer.print(" lot: " + arcStatusRecord.lotId());
        printer.print(" manifest: " + manifestURI.uri());
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
    }

    private static @NotNull List<SourceDataset> findSourceDatasetsFor(ArcMeta arcMeta, List<String> sourceNames) {
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

        if (datasets.isEmpty()) {
            throw new IllegalArgumentException("no sources found in arc " + arcMeta.arc().name() + " for source names " + sourceNames);
        }
        return datasets;
    }

    private static @NotNull List<Tuple2<Moment, Moment>> toRanges(List<String> lots) {
        return lots.stream()
                .map(IntervalUnitParser::convert)
                .map(l -> new Tuple2<>(l, l))
                .toList();
    }

    private @NotNull List<ArcStatusScanner> scanViaRange(Predicate<ArcRecord> arcRecordPredicate, String profile, Moment earliest, Moment latest, Supplier<Optional<Predicate<ArcState>>> arcStateSupplier, boolean fillGaps) {
        List<ArcStatusScanner> list;
        try (Stream<ArcRecord> arcStream = arcsCommand.listAllArcs(arcRecordPredicate)) {
            // convert this to a list of lots within an arc
            list = arcStream
                    .map(arcRecord -> scannerOrNull(arcRecord, profile, earliest, latest, arcStateSupplier, fillGaps))
                    .filter(Objects::nonNull)
                    .toList();
        }
        return list;
    }
}
