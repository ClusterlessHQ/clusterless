/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report.scanner;

import clusterless.cls.substrate.aws.report.ArcRecord;
import clusterless.cls.substrate.aws.report.ArcStatusRecord;
import clusterless.cls.substrate.aws.report.ArcStatusSummaryRecord;
import clusterless.cls.substrate.aws.sdk.ClientBase;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.ArcStateURI;
import clusterless.cls.util.Moment;
import clusterless.commons.temporal.IntervalUnits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.stream.Stream;

public class ArcScanner {
    private static final Logger LOG = LoggerFactory.getLogger(ArcScanner.class);
    private final String profile;
    private final ArcRecord arcRecord;
    private final Moment earliest;
    private final Moment latest;
    private final ArcStateURI arcStateURI;
    private final TemporalUnit temporalUnit;
    private final String startLotInclusive;
    private final String endLotInclusive;
    private final String endLotExclusive;

    public ArcScanner(String profile, ArcRecord arcRecord, Moment earliest, Moment latest) {
        this.profile = profile;
        this.arcRecord = arcRecord;
        this.earliest = earliest;
        this.latest = latest;

        LOG.info("creating scanner for: {}", arcRecord);

        arcStateURI = ArcStateURI.builder()
                .withPlacement(arcRecord.placement())
                .withProject(arcRecord.project())
                .withArcName(arcRecord.name())
                .build();

        temporalUnit = findTemporalKeyFor(arcStateURI);

        LOG.info("using temporal unit: {}", temporalUnit);

        startLotInclusive = IntervalUnits.formatter(temporalUnit).format(earliest.instant());
        endLotInclusive = IntervalUnits.formatter(temporalUnit).format(latest.instant().minus(1, temporalUnit));
        endLotExclusive = IntervalUnits.formatter(temporalUnit).format(latest.instant());
    }

    public ArcStatusSummaryRecord summarizeScan() {
        long count = Duration.between(earliest.instant(), latest.instant()).dividedBy(temporalUnit.getDuration());

        ArcStatusSummaryRecord summaryRecord = ArcStatusSummaryRecord.builder()
                .withArcRecord(arcRecord)
                .withTemporalUnit(temporalUnit)
                .withStartLot(startLotInclusive)
                .withEndLot(endLotInclusive)
                .withIntervals(count)
                .build();

        scan().forEach(summaryRecord::addState);

        return summaryRecord;
    }

    public Stream<ArcStatusRecord> scan() {
        S3 s3 = new S3(profile);
        LOG.info("using profile: {}", profile);

        URI path = arcStateURI.uriPath();
        // since no state information is associated, the lot id is inclusive as the next actual key is the object
        URI startInclusive = arcStateURI.withLot(startLotInclusive).uriPath();
        URI endExclusive = arcStateURI.withLot(endLotExclusive).uriPath();
        final ClientBase<?>.Response[] response = new ClientBase.Response[]{null};

        LOG.info("scanning earliest: {}, latest: {}", startInclusive, endExclusive);
        S3.Responses responses = s3.listObjectsIterable(path, startInclusive);

        Stream<ArcStatusRecord> resultStream = s3.listChildrenStream(responses, endExclusive, r -> response[0] = r)
                .map(ArcStateURI::parse)
                .map(uri -> new ArcStatusRecord(arcRecord, uri.lotId(), uri.state()));

        if (response[0] != null) {
            response[0].isSuccessOrThrow(e -> new RuntimeException("unable to list objects at: " + path, e));
        }

        return resultStream;
    }

    protected TemporalUnit findTemporalKeyFor(ArcStateURI stateURI) {

        // discover interval
        S3 s3 = new S3(profile, 1);
        S3.Response response = s3.listPaths(stateURI.uriPath());

        if (!response.isSuccess()) {
            LOG.info("no arc states found: {}", stateURI);
            throw new IllegalStateException("no arc states found: " + stateURI);
        }

        List<String> paths = s3.listChildren(response);

        if (paths.isEmpty()) {
            LOG.info("no arc states found: {}", stateURI);
            throw new IllegalStateException("no arc states found: " + stateURI);
        }

        ArcStateURI found = ArcStateURI.parse(paths.get(0));

        return IntervalUnits.findDurationWithin(found.lotId())
                .orElseThrow(() -> new IllegalStateException("no TemporalUnit found: " + found.lotId()));
    }
}
