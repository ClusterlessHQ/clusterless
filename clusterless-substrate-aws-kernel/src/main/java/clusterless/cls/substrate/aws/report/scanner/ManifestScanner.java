/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report.scanner;

import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.substrate.aws.report.DatasetRecord;
import clusterless.cls.substrate.aws.report.DatasetStatusRecord;
import clusterless.cls.substrate.aws.report.DatasetStatusSummaryRecord;
import clusterless.cls.substrate.uri.ManifestURI;
import clusterless.cls.substrate.uri.StateURI;
import clusterless.cls.util.Moment;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ManifestScanner extends Scanner<DatasetRecord, DatasetStatusRecord, DatasetStatusSummaryRecord, ManifestState> {

    public ManifestScanner(String profile, DatasetRecord datasetRecord, Moment earliest, Moment latest) {
        super(profile, datasetRecord, earliest, latest);
    }

    @Override
    protected StateURI<?, ?> createStateURIFrom(DatasetRecord record) {
        return ManifestURI.builder()
                .withPlacement(record.placement())
                .withDataset(record.dataset())
                .build();
    }

    @NotNull
    protected Stream<DatasetStatusRecord> parseStreamIntoUri(Stream<String> resultStream) {
        return resultStream.map(ManifestURI::parse)
                .map(uri -> new DatasetStatusRecord(record, uri.lotId(), uri.state()));
    }

    @Override
    protected @NotNull DatasetStatusSummaryRecord createSummaryRecord(long count) {
        return DatasetStatusSummaryRecord.builder()
                .withDatasetRecord(record)
                .withTemporalUnit(temporalUnit)
                .withStartLot(startLotInclusive)
                .withEndLot(endLotInclusive)
                .withIntervals(count)
                .build();
    }

    @Override
    protected ManifestURI parseStateURU(String uri) {
        return ManifestURI.parse(uri);
    }

    @Override
    protected String objectName() {
        return ".json";
    }
}
