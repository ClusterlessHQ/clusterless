/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.model.Struct;
import clusterless.cls.model.state.ArcState;
import clusterless.commons.temporal.IntervalUnits;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.EnumMultiset;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalUnit;

@JsonPropertyOrder({
        "arc",
        "temporalUnit",
        "startLot",
        "firstLot",
        "startGap",
        "endLot",
        "lastLot",
        "endGap",
        "intervals",
        "running",
        "partial",
        "missing",
        "complete",
        "gaps",
        "total"
})
public class ArcStatusSummaryRecord implements Struct {
    @JsonUnwrapped
    @JsonProperty("arc")
    ArcRecord arcRecord;
    TemporalUnit temporalUnit;
    String startLot;
    String endLot;
    long intervals;

    @JsonIgnore
    EnumMultiset<ArcState> states = EnumMultiset.create(ArcState.class);

    String firstLot;
    String lastLot;

    public static Builder builder() {
        return Builder.builder();
    }

    public ArcRecord arcRecord() {
        return arcRecord;
    }

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

    public EnumMultiset<ArcState> states() {
        return states;
    }

    @JsonProperty("startGap")
    public int startGap() {
        return gap(startLot, firstLot);
    }

    @JsonProperty("endGap")
    public int endGap() {
        return gap(lastLot, endLot);
    }

    private int gap(String lhs, String rhs) {
        if (lhs.equals(rhs)) {
            return 0;
        }

        DateTimeFormatter formatter = IntervalUnits.formatter(temporalUnit);

        TemporalAccessor begin = formatter.parse(lhs);
        TemporalAccessor end = formatter.parse(rhs);

        return (int) Duration.between(begin.query(Instant::from), end.query(Instant::from)).dividedBy(temporalUnit.getDuration());
    }

    @JsonProperty("running")
    public int running() {
        return states.count(ArcState.running);
    }

    @JsonProperty("complete")
    public int complete() {
        return states.count(ArcState.complete);
    }

    @JsonProperty("partial")
    public int partial() {
        return states.count(ArcState.partial);
    }

    @JsonProperty("missing")
    public int missing() {
        return states.count(ArcState.missing);
    }

    @JsonProperty("gaps")
    public long gaps() {
        return intervals - states.size();
    }

    @JsonProperty("total")
    public int total() {
        return states.size();
    }

    public long intervals() {
        return intervals;
    }

    public void addState(ArcState state) {
        states.add(state);
    }

    public void addState(ArcStatusRecord arcStatusRecord) {
        addState(arcStatusRecord.arcState());
        if (firstLot == null) {
            firstLot = arcStatusRecord.lotId();
        }
        lastLot = arcStatusRecord.lotId();
    }

    public static final class Builder {
        ArcRecord arcRecord;
        TemporalUnit temporalUnit;
        String startLot;
        String endLot;
        long intervals;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withArcRecord(ArcRecord arcRecord) {
            this.arcRecord = arcRecord;
            return this;
        }

        public Builder withTemporalUnit(TemporalUnit temporalUnit) {
            this.temporalUnit = temporalUnit;
            return this;
        }

        public Builder withStartLot(String startLot) {
            this.startLot = startLot;
            return this;
        }

        public Builder withEndLot(String endLot) {
            this.endLot = endLot;
            return this;
        }

        public Builder withIntervals(long intervals) {
            this.intervals = intervals;
            return this;
        }

        public ArcStatusSummaryRecord build() {
            ArcStatusSummaryRecord arcStatusSummaryRecord = new ArcStatusSummaryRecord();
            arcStatusSummaryRecord.startLot = this.startLot;
            arcStatusSummaryRecord.intervals = this.intervals;
            arcStatusSummaryRecord.temporalUnit = this.temporalUnit;
            arcStatusSummaryRecord.arcRecord = this.arcRecord;
            arcStatusSummaryRecord.endLot = this.endLot;
            return arcStatusSummaryRecord;
        }
    }
}



