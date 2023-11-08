/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.report.DatasetsCommandOptions;
import clusterless.cls.model.deploy.Dataset;
import clusterless.cls.substrate.aws.report.reporter.Reporter;
import clusterless.cls.substrate.aws.sdk.S3;
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

    private static int compare(Dataset o1, Dataset o2) {
        int i = o1.name().compareTo(o2.name());

        if (i == 0) {
            return 0;
        }

        if (o1.version() == null || o2.version() == null) {
            return i;
        }

        return o1.version().compareTo(o2.version());
    }

    @Override
    public Integer call() throws Exception {
        Stream<DatasetRecord> records = listAllDatasets(commandOptions);

        Reporter<DatasetRecord> reporter = Reporter.instance(kernel().printer(), DatasetRecord.class);

        reporter.report(records);

        return 0;
    }

    @NotNull
    public Stream<DatasetRecord> listAllDatasets(Predicate<DatasetRecord> datasetRecordPredicate) {
        S3 s3 = new S3(commandOptions.profile());

        return listAllDatasets(commandOptions)
                .map(r -> Map.entry(r.placement, listAllDatasetKeys(s3, r)))
                .flatMap(e -> e.getValue().stream().map(a -> Map.entry(e.getKey(), DatasetURI.parse("/" + a))))
                .map(e -> new DatasetRecord(e.getKey(), e.getValue().dataset()))
                .filter(datasetRecordPredicate);
    }

    private static List<String> listAllDatasetKeys(S3 s3, DatasetRecord datasetRecord) {
        URI uri = DatasetURI.builder()
                .withPlacement(datasetRecord.placement())
                .withDataset(datasetRecord.dataset())
                .build()
                .uriPrefix();

        S3.Response response = s3.listObjects(uri);

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to list projects in: %s, %s", uri, r.errorMessage())
        );

        return s3.listChildren(response);
    }

}
