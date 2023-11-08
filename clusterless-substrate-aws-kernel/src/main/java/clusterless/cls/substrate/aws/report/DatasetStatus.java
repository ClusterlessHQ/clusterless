/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.command.report.DatasetStatusCommandOption;
import clusterless.cls.substrate.aws.report.reporter.Reporter;
import clusterless.cls.substrate.aws.report.scanner.ManifestScanner;
import clusterless.cls.util.Moment;
import picocli.CommandLine;

import java.util.concurrent.Callable;
import java.util.function.Predicate;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "status",
        description = "List the status of all datasets in the range."
)
public class DatasetStatus implements Callable<Integer> {
    @CommandLine.ParentCommand
    Datasets datasetsCommand;

    @CommandLine.Mixin
    DatasetStatusCommandOption datasetStatusCommandOption = new DatasetStatusCommandOption();

    @Override
    public Integer call() throws Exception {
        datasetsCommand.commandOptions.setProfile(datasetStatusCommandOption.profile());
        datasetsCommand.commandOptions.setAccount(datasetStatusCommandOption.account());
        datasetsCommand.commandOptions.setRegion(datasetStatusCommandOption.region());
        datasetsCommand.commandOptions.setStage(datasetStatusCommandOption.stage());

        Predicate<DatasetRecord> datasetRecordPredicate = datasetRecord -> true;

        if (!datasetStatusCommandOption.names().isEmpty()) {
            datasetRecordPredicate = datasetRecord -> datasetStatusCommandOption.names().contains(datasetRecord.dataset().name());
        }

        String profile = datasetsCommand.commandOptions.profile();
        Moment earliest = datasetStatusCommandOption.earliest();
        Moment latest = datasetStatusCommandOption.latest();

        if (datasetStatusCommandOption.list()) {
            Reporter<DatasetStatusRecord> reporter = Reporter.instance(datasetsCommand.kernel().printer(), DatasetStatusRecord.class);

            try (Stream<DatasetRecord> datasetStream = datasetsCommand.listAllDatasets(datasetRecordPredicate)) {
                reporter.report(datasetStream
                        .map(datasetRecord -> new ManifestScanner(profile, datasetRecord, earliest, latest))
                        .flatMap(ManifestScanner::scan)
                );
            }
        } else {
            Reporter<DatasetStatusSummaryRecord> reporter = Reporter.instance(datasetsCommand.kernel().printer(), DatasetStatusSummaryRecord.class);

            try (Stream<DatasetRecord> datasetRecordStream = datasetsCommand.listAllDatasets(datasetRecordPredicate)) {
                reporter.report(datasetRecordStream
                        .map(arcRecord -> new ManifestScanner(profile, arcRecord, earliest, latest))
                        .map(ManifestScanner::summarizeScan)
                );
            }
        }

        return 0;
    }

}
