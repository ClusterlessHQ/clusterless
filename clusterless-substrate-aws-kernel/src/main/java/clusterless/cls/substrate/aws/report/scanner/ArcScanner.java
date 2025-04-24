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
import clusterless.cls.util.Moment;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ArcScanner extends Scanner<ArcRecord, ArcStatusRecord, ArcStatusSummaryRecord, ArcState> {

    private final Supplier<Optional<Predicate<ArcState>>> arcStateSupplier;

    public static ArcScanner scannerOrNull(ArcRecord arcRecord, String profile, Moment earliest, Moment latest, Supplier<Optional<Predicate<ArcState>>> arcStateSupplier) {
        try {
            return new ArcScanner(profile, arcRecord, earliest, latest, arcStateSupplier);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public ArcScanner(String profile, ArcRecord arcRecord, Moment earliest, Moment latest, Supplier<Optional<Predicate<ArcState>>> arcStateSupplier) {
        super(profile, arcRecord, earliest, latest);
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
    protected Stream<ArcStatusRecord> parseStreamIntoUri(Stream<String> resultStream) {
        Stream<ArcStateURI> arcStateURIStream = resultStream.map(ArcStateURI::parse);

        Optional<Predicate<ArcState>> supplied = arcStateSupplier.get();
        if (supplied.isPresent()) {
            Predicate<ArcState> predicate = supplied.get();
            arcStateURIStream = arcStateURIStream.filter(uri -> predicate.test(uri.state()));
        }

        // detect gaps here and invert the stream if filling gaps
        return arcStateURIStream
                .map(uri -> new ArcStatusRecord(record, uri.lotId(), uri.state()));
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
