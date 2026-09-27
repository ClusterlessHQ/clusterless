/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.report.DatasetsCommandOptions;
import clusterless.cls.substrate.aws.report.reporter.Reporter;
import clusterless.cls.substrate.uri.DatasetURI;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 */
@CommandLine.Command(
        name = "datasets",
        description = "List all datasets",
        subcommands = {
                DatasetStatus.class
        }
)
public class Datasets extends Reports implements Callable<Integer> {

    @CommandLine.Mixin
    DatasetsCommandOptions commandOptions = new DatasetsCommandOptions();

    @Override
    public Integer call() throws Exception {
        Stream<DatasetRecord> records = listAllDatasets(commandOptions);

        Reporter<DatasetRecord> reporter = Reporter.instance(kernel().printer(), DatasetRecord.class);

        reporter.report(records);

        return 0;
    }

    @NotNull
    public Stream<DatasetRecord> listAllDatasets(Predicate<DatasetRecord> datasetRecordPredicate) {
        String profile = commandOptions.profile();

        return listAllDatasets(commandOptions)
                .map(r -> Map.entry(r.placement, listAllDatasetKeys(profile, r)))
                .flatMap(e -> e.getValue().stream().map(a -> Map.entry(e.getKey(), DatasetURI.parse("/" + a))))
                .map(e -> new DatasetRecord(e.getKey(), e.getValue().dataset()))
                .filter(datasetRecordPredicate);
    }

    private List<String> listAllDatasetKeys(String profile, DatasetRecord datasetRecord) {
        URI uri = DatasetURI.builder()
                .withPlacement(datasetRecord.placement())
                .withDataset(datasetRecord.dataset())
                .build()
                .uriPrefix();

        return s3For.apply(profile, datasetRecord.placement().region()).listAllChildren(uri);
    }

}
