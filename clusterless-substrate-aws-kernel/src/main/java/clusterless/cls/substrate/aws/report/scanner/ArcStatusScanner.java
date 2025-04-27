/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
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
import clusterless.cls.util.LotStream;
import clusterless.cls.util.Moment;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ArcStatusScanner extends Scanner<ArcRecord, ArcStatusRecord, ArcStatusSummaryRecord, ArcState> {

    private final Supplier<Optional<Predicate<ArcState>>> arcStateSupplier;

    public static ArcStatusScanner scannerOrNull(ArcRecord arcRecord, String profile, Moment earliest, Moment latest, Supplier<Optional<Predicate<ArcState>>> arcStateSupplier, boolean fillGaps) {
        try {
            return new ArcStatusScanner(profile, arcRecord, earliest, latest, arcStateSupplier, fillGaps);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public ArcStatusScanner(String profile, ArcRecord arcRecord, Moment earliest, Moment latest, Supplier<Optional<Predicate<ArcState>>> arcStateSupplier, boolean includeGaps) {
        super(profile, arcRecord, earliest, latest, includeGaps);
        this.arcStateSupplier = arcStateSupplier;
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
    protected Stream<ArcStatusRecord> parseUriStreamIntoStatusRec(Stream<String> resultStream) {

        Stream<ArcStatusRecord> arcStatusRecordStream;

        arcStatusRecordStream = resultStream.map(ArcStateURI::parse)
                .map(uri -> new ArcStatusRecord(record, uri.lotId(), uri.state()));

        if (fillGaps) {
            // there has to be a better way to zip together ordered streams and remove any dupes by a predicate or
            // bifunction
            arcStatusRecordStream = StreamEx.of(LotStream.stream(startLotInclusive, endLotExclusive))
                    .map(lot -> new ArcStatusRecord(record, lot, null))
                    .append(arcStatusRecordStream)
                    .sortedBy(ArcStatusRecord::lotId)
                    .collapse(
                            (l, r) -> l.lotId().equals(r.lotId()),
                            (l, r) -> l.state() == null ? r : l
                    );
        }

        Optional<Predicate<ArcState>> supplied = arcStateSupplier.get();
        if (supplied.isPresent()) {
            Predicate<ArcState> predicate = supplied.get();
            return arcStatusRecordStream.filter(statusRecord -> predicate.test(statusRecord.state()));
        }

        return arcStatusRecordStream;

    }

    @Override
    @NotNull
    protected ArcStatusSummaryRecord createSummaryRecord(long count) {
        return ArcStatusSummaryRecord.builder()
                .withArcRecord(record)
                .withTemporalUnit(temporalUnit)
                .withEarliestLot(startLotInclusive)
                .withLatestLot(endLotInclusive)
                .withRangeIntervals(count)
                .build();
    }

    @Override
    protected ArcStateURI parseStateURI(String uri) {
        return ArcStateURI.parse(uri);
    }

    @Override
    protected String objectName() {
        return ".arc";
    }
}
