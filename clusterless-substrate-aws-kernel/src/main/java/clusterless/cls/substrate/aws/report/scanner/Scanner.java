/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report.scanner;

import clusterless.cls.model.HasDisplay;
import clusterless.cls.model.State;
import clusterless.cls.substrate.aws.report.StatusRecord;
import clusterless.cls.substrate.aws.report.StatusSummaryRecord;
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
import java.util.Optional;
import java.util.stream.Stream;

public abstract class Scanner<Rec extends HasDisplay, StatusRec extends StatusRecord<S>, StatusSummaryRec extends StatusSummaryRecord<S>, S extends State> {
    protected static final Logger LOG = LoggerFactory.getLogger(ArcStatusScanner.class);
    protected final String profile;
    protected final Rec record;
    protected final StateURI<?, ?> stateURI;
    protected final TemporalUnit temporalUnit;
    protected final String startLotInclusive;
    protected final String endLotInclusive;
    protected final String endLotExclusive;
    protected final Instant earliestInstant;
    protected final Instant latestInstant;
    protected final boolean fillGaps;

    public Scanner(String profile, Rec record, Moment earliest, Moment latest, boolean fillGaps) {
        this.profile = profile;
        this.record = record;
        this.fillGaps = fillGaps;
        LOG.info("creating {} scanner for: {}", scannerType(), record.display());

        this.stateURI = createStateURIFrom(record);
        this.temporalUnit = findTemporalKeyFor(this.stateURI, earliest, latest);

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

    protected abstract String scannerType();

    public String profile() {
        return profile;
    }

    public Rec record() {
        return record;
    }

    protected abstract StateURI<?, ?> createStateURIFrom(Rec record);

    /**
     * Creates the S3 client wrapper used for listing, in the region of the record's placement. Called from the
     * constructor, after {@link #stateURI} is set, as well as {@link #scan()}, so overrides must not depend on
     * subclass state.
     */
    protected S3 createS3(int maxKeys) {
        return new S3(profile, stateURI.placement().region(), maxKeys);
    }

    /**
     * The returned stream holds an S3 client until closed; consume it in try-with-resources. A listing failure
     * throws while the stream is consumed, naming the listed path and the cause.
     */
    public Stream<StatusRec> scan() {
        S3 s3 = createS3(S3.DEFAULT_MAX_KEYS);
        LOG.info("using profile: {}", profile);

        URI path = stateURI.uriPath();
        // since no state information is associated, the lot id is inclusive as the next actual key is the object
        URI startInclusive = stateURI.withLot(startLotInclusive).uriPath();
        URI endExclusive = stateURI.withLot(endLotExclusive).uriPath();

        LOG.info("scanning earliest inclusive: {}, latest exclusive: {}", startInclusive, endExclusive);
        S3.Responses responses = s3.listObjectsIterable(path, startInclusive);

        Stream<String> resultStream = s3.listChildrenStream(responses, path, endExclusive, objectName());

        try {
            return parseUriStreamIntoStatusRec(resultStream);
        } catch (RuntimeException exception) {
            resultStream.close();
            throw exception;
        }
    }

    @NotNull
    protected abstract Stream<StatusRec> parseUriStreamIntoStatusRec(Stream<String> resultStream);

    protected TemporalUnit findTemporalKeyFor(StateURI<?, ?> stateURI, Moment earliest, Moment latest) {
        Optional<TemporalUnit> temporalUnit = IntervalUnits.findDurationWithin(earliest.moment())
                .or(() -> IntervalUnits.findDurationWithin(latest.moment()));

        return temporalUnit.orElseGet(() -> findTemporalKeyFor(stateURI));
    }

    protected TemporalUnit findTemporalKeyFor(StateURI<?, ?> stateURI) {
        // discover interval
        S3 s3 = createS3(1);
        S3.Response response = s3.listPaths(stateURI.uriPath());

        // IllegalStateException means nothing to report and callers skip the record, so a failed listing
        // must throw something else
        response.isSuccessOrThrow(r -> "unable to list states at: " + stateURI + ": " + s3.error(r), RuntimeException::new);

        List<String> paths = s3.listChildren(response);

        if (paths.isEmpty()) {
            LOG.info("no arc states found: {}", stateURI);
            throw new IllegalStateException("no arc states found: " + stateURI);
        }

        LOG.info("parsing: {}", paths.get(0));
        StateURI<?, ?> found = parseStateURI(paths.get(0));

        return IntervalUnits.findDurationWithin(found.lotId())
                .orElseThrow(() -> new IllegalStateException("no TemporalUnit found: " + found.lotId()));
    }

    public StatusSummaryRec summarizeScan() {
        long count = Duration.between(earliestInstant, latestInstant).dividedBy(temporalUnit.getDuration());

        StatusSummaryRec summaryRecord = createSummaryRecord(count);

        try (Stream<StatusRec> stream = scan()) {
            stream.forEach(summaryRecord::addStateRecord);
        }

        return summaryRecord;
    }

    @NotNull
    protected abstract StatusSummaryRec createSummaryRecord(long count);

    protected abstract StateURI<?, ?> parseStateURI(String uri);

    protected abstract String objectName();
}
