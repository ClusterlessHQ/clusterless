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
    String startLot;
    String endLot;
    long intervals;
    String firstLot;
    String lastLot;

    public TemporalUnit temporalUnit() {
        return temporalUnit;
    }

    public String startLot() {
        return startLot;
    }

    public String endLot() {
        return endLot;
    }

    public String firstLot() {
        return firstLot;
    }

    public String lastLot() {
        return lastLot;
    }

    @JsonProperty("startGap")
    public Integer startGap() {
        return gap(startLot, firstLot);
    }

    @JsonProperty("endGap")
    public Integer endGap() {
        return gap(lastLot, endLot);
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

    @JsonProperty("intervals")
    public long intervals() {
        return intervals;
    }

    @JsonProperty("gaps")
    public long gaps() {
        return intervals - statesSize();
    }

    @JsonProperty("total")
    public int total() {
        return statesSize();
    }

    protected abstract int statesSize();

    public abstract void addState(S state);

    public void addStateRecord(StatusRecord<S> statusRecord) {
        addState(statusRecord.state());
        if (firstLot == null) {
            firstLot = statusRecord.lotId();
        }
        lastLot = statusRecord.lotId();
    }
}
