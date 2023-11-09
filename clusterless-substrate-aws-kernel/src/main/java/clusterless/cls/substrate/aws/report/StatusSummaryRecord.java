/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.model.State;
import clusterless.cls.model.Struct;
import clusterless.commons.temporal.IntervalUnits;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;

public abstract class StatusSummaryRecord<S extends State> implements Struct {
    TemporalUnit temporalUnit;
    String earliestLot;
    String latestLot;
    long rangeIntervals;
    String firstFoundLot;
    String lastFoundLot;

    public TemporalUnit temporalUnit() {
        return temporalUnit;
    }

    @JsonProperty("earliest")
    public String earliestLot() {
        return earliestLot;
    }

    @JsonProperty("latest")
    public String latestLot() {
        return latestLot;
    }

    @JsonProperty("earliestFound")
    public String firstFoundLot() {
        return firstFoundLot;
    }

    @JsonProperty("latestFound")
    public String lastFoundLot() {
        return lastFoundLot;
    }

    @JsonProperty("earliestGap")
    public Integer earliestGap() {
        return gap(earliestLot, firstFoundLot);
    }

    @JsonProperty("latestGap")
    public Integer latestGap() {
        return gap(lastFoundLot, latestLot);
    }

    private Integer gap(String lhs, String rhs) {
        if (lhs == null || rhs == null) {
            return null;
        }

        if (lhs.equals(rhs)) {
            return 0;
        }

        DateTimeFormatter formatter = IntervalUnits.formatter(temporalUnit);

        TemporalAccessor begin = formatter.parse(lhs);
        TemporalAccessor end = formatter.parse(rhs);

        return (int) Duration.between(begin.query(Instant::from), end.query(Instant::from)).dividedBy(temporalUnit.getDuration());
    }

    @JsonProperty("range")
    public long rangeIntervals() {
        return rangeIntervals;
    }

    @JsonProperty("rangeGap")
    public long rangeGap() {
        return rangeIntervals - statesSize();
    }

    @JsonProperty("totalFound")
    public int total() {
        return statesSize();
    }

    protected abstract int statesSize();

    public abstract void addState(S state);

    public void addStateRecord(StatusRecord<S> statusRecord) {
        addState(statusRecord.state());
        if (firstFoundLot == null) {
            firstFoundLot = statusRecord.lotId();
        }
        lastFoundLot = statusRecord.lotId();
    }
}
