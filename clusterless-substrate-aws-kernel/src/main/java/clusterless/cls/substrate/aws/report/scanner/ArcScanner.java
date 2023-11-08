/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report.scanner;

import clusterless.cls.model.state.ArcState;
import clusterless.cls.substrate.aws.report.ArcRecord;
import clusterless.cls.substrate.aws.report.ArcStatusRecord;
import clusterless.cls.substrate.aws.report.ArcStatusSummaryRecord;
import clusterless.cls.substrate.uri.ArcStateURI;
import clusterless.cls.substrate.uri.StateURI;
import clusterless.cls.util.Moment;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ArcScanner extends Scanner<ArcRecord, ArcStatusRecord, ArcStatusSummaryRecord, ArcState> {

    public ArcScanner(String profile, ArcRecord arcRecord, Moment earliest, Moment latest) {
        super(profile, arcRecord, earliest, latest);
    }

    @Override
    protected StateURI<?, ?> createStateURIFrom(ArcRecord record) {
        return ArcStateURI.builder()
                .withPlacement(record.placement())
                .withProject(record.project())
                .withArcName(record.name())
                .build();
    }

    @NotNull
    protected Stream<ArcStatusRecord> parseStreamIntoUri(Stream<String> resultStream) {
        return resultStream.map(ArcStateURI::parse)
                .map(uri -> new ArcStatusRecord(record, uri.lotId(), uri.state()));
    }

    @Override
    @NotNull
    protected ArcStatusSummaryRecord createSummaryRecord(long count) {
        return ArcStatusSummaryRecord.builder()
                .withArcRecord(record)
                .withTemporalUnit(temporalUnit)
                .withStartLot(startLotInclusive)
                .withEndLot(endLotInclusive)
                .withIntervals(count)
                .build();
    }

    @Override
    protected ArcStateURI parseStateURU(String uri) {
        return ArcStateURI.parse(uri);
    }

    @Override
    protected String objectName() {
        return ".arc";
    }
}
