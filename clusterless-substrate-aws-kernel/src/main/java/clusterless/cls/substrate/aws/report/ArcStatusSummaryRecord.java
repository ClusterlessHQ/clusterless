/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.model.state.ArcState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.EnumMultiset;

import java.time.temporal.TemporalUnit;

@JsonPropertyOrder({
        "arc",
        "temporalUnit",
        "earliest",
        "earliestFound",
        "earliestGap",
        "latest",
        "latestFound",
        "latestGap",
        "running",
        "partial",
        "missing",
        "complete",
        "totalFound",
        "range",
        "rangeGap"
})
public class ArcStatusSummaryRecord extends StatusSummaryRecord<ArcState> {
    @JsonUnwrapped
    @JsonProperty("arc")
    ArcRecord arcRecord;

    @JsonIgnore
    EnumMultiset<ArcState> states = EnumMultiset.create(ArcState.class);

    public static Builder builder() {
        return Builder.builder();
    }

    public ArcRecord arcRecord() {
        return arcRecord;
    }

    public EnumMultiset<ArcState> states() {
        return states;
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

    @Override
    protected int statesSize() {
        return states.size();
    }

    @Override
    public void addState(ArcState state) {
        states.add(state);
    }

    public static final class Builder {
        ArcRecord arcRecord;
        TemporalUnit temporalUnit;
        String earliestLot;
        String latestLot;
        long rangeIntervals;
        String firstFoundLot;
        String lastFoundLot;

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

        public Builder withEarliestLot(String earliestLot) {
            this.earliestLot = earliestLot;
            return this;
        }

        public Builder withLatestLot(String latestLot) {
            this.latestLot = latestLot;
            return this;
        }

        public Builder withRangeIntervals(long rangeIntervals) {
            this.rangeIntervals = rangeIntervals;
            return this;
        }

        public Builder withFirstFoundLot(String firstFoundLot) {
            this.firstFoundLot = firstFoundLot;
            return this;
        }

        public Builder withLastFoundLot(String lastFoundLot) {
            this.lastFoundLot = lastFoundLot;
            return this;
        }

        public ArcStatusSummaryRecord build() {
            ArcStatusSummaryRecord arcStatusSummaryRecord = new ArcStatusSummaryRecord();
            arcStatusSummaryRecord.arcRecord = this.arcRecord;
            arcStatusSummaryRecord.earliestLot = this.earliestLot;
            arcStatusSummaryRecord.latestLot = this.latestLot;
            arcStatusSummaryRecord.lastFoundLot = this.lastFoundLot;
            arcStatusSummaryRecord.firstFoundLot = this.firstFoundLot;
            arcStatusSummaryRecord.rangeIntervals = this.rangeIntervals;
            arcStatusSummaryRecord.temporalUnit = this.temporalUnit;
            return arcStatusSummaryRecord;
        }
    }
}



