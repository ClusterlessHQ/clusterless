/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report.scanner;

import clusterless.cls.model.State;
import clusterless.cls.substrate.aws.report.StatusRecord;
import clusterless.cls.substrate.aws.report.StatusSummaryRecord;
import clusterless.cls.substrate.aws.sdk.ClientBase;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.StateURI;
import clusterless.cls.util.Moment;
import clusterless.commons.temporal.IntervalUnits;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.stream.Stream;

public abstract class Scanner<Rec, StatusRec extends StatusRecord<S>, StatusSummaryRec extends StatusSummaryRecord<S>, S extends State> {
    protected static final Logger LOG = LoggerFactory.getLogger(ArcScanner.class);
    protected final String profile;
    protected final Rec record;
    protected final StateURI<?, ?> stateURI;
    protected final TemporalUnit temporalUnit;
    protected final String startLotInclusive;
    protected final String endLotInclusive;
    protected final String endLotExclusive;
    private final Instant earliestInstant;
    private final Instant latestInstant;

    public Scanner(String profile, Rec record, Moment earliest, Moment latest) {
        this.profile = profile;
        this.record = record;
        LOG.info("creating scanner for: {}", record);

        this.stateURI = createStateURIFrom(record);
        this.temporalUnit = findTemporalKeyFor(this.stateURI);

        LOG.info("using temporal unit: {}", this.temporalUnit);

        LOG.info("using moment earliest: {}, latest: {}", earliest.print(), latest.print());

        this.earliestInstant = earliest.instant();
        boolean sameInterval = latest.instant().truncatedTo(temporalUnit).equals(earliestInstant.truncatedTo(temporalUnit));
        this.latestInstant = sameInterval ? latest.instant().plus(1, temporalUnit) : latest.instant();

        LOG.info("using instant earliest: {}, latest: {}, latest adjusted: {}", earliestInstant, latestInstant, sameInterval);

        this.startLotInclusive = IntervalUnits.formatter(this.temporalUnit).format(this.earliestInstant);
        this.endLotExclusive = IntervalUnits.formatter(this.temporalUnit).format(this.latestInstant);
        this.endLotInclusive = IntervalUnits.formatter(this.temporalUnit).format(this.latestInstant.minus(1, temporalUnit));

        LOG.info("using lot earliest: {}, latest: {}", startLotInclusive, endLotExclusive);
    }

    protected abstract StateURI<?, ?> createStateURIFrom(Rec record);

    public Stream<StatusRec> scan() {
        S3 s3 = new S3(profile);
        LOG.info("using profile: {}", profile);

        URI path = stateURI.uriPath();
        // since no state information is associated, the lot id is inclusive as the next actual key is the object
        URI startInclusive = stateURI.withLot(startLotInclusive).uriPath();
        URI endExclusive = stateURI.withLot(endLotExclusive).uriPath();
        final ClientBase<?>.Response[] response = new ClientBase.Response[]{null};

        LOG.info("scanning earliest: {}, latest: {}", startInclusive, endExclusive);
        S3.Responses responses = s3.listObjectsIterable(path, startInclusive);

        Stream<String> resultStream = s3.listChildrenStream(responses, endExclusive, objectName(), r -> response[0] = r);

        try {
            return parseStreamIntoUri(resultStream);
        } finally {
            if (response[0] != null) {
                response[0].isSuccessOrThrow(e -> new RuntimeException("unable to list objects at: " + path, e));
            }
        }
    }

    @NotNull
    protected abstract Stream<StatusRec> parseStreamIntoUri(Stream<String> resultStream);

    protected TemporalUnit findTemporalKeyFor(StateURI<?, ?> stateURI) {
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

        LOG.info("parsing: {}", paths.get(0));
        StateURI<?, ?> found = parseStateURU(paths.get(0));

        return IntervalUnits.findDurationWithin(found.lotId())
                .orElseThrow(() -> new IllegalStateException("no TemporalUnit found: " + found.lotId()));
    }

    public StatusSummaryRec summarizeScan() {
        long count = Duration.between(earliestInstant, latestInstant).dividedBy(temporalUnit.getDuration());

        StatusSummaryRec summaryRecord = createSummaryRecord(count);

        scan().forEach(summaryRecord::addStateRecord);

        return summaryRecord;
    }

    @NotNull
    protected abstract StatusSummaryRec createSummaryRecord(long count);

    protected abstract StateURI<?, ?> parseStateURU(String uri);

    protected abstract String objectName();
}
