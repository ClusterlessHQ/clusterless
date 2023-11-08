/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.model.manifest.ManifestState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.EnumMultiset;

import java.time.temporal.TemporalUnit;


@JsonPropertyOrder({
        "manifest",
        "temporalUnit",
        "startLot",
        "firstLot",
        "startGap",
        "endLot",
        "lastLot",
        "endGap",
        "intervals",
        "removed",
        "partial",
        "empty",
        "complete",
        "gaps",
        "total"
})
public class DatasetStatusSummaryRecord extends StatusSummaryRecord<ManifestState> {
    @JsonUnwrapped
    @JsonProperty("manifest")
    DatasetRecord datasetRecord;

    @JsonIgnore
    EnumMultiset<ManifestState> states = EnumMultiset.create(ManifestState.class);

    public static Builder builder() {
        return Builder.builder();
    }

    public DatasetRecord datasetRecord() {
        return datasetRecord;
    }

    @Override
    protected int statesSize() {
        return states.size();
    }

    public void addState(ManifestState state) {
        states.add(state);
    }

    @JsonProperty("empty")
    public int running() {
        return states.count(ManifestState.empty);
    }

    @JsonProperty("complete")
    public int complete() {
        return states.count(ManifestState.complete);
    }

    @JsonProperty("partial")
    public int partial() {
        return states.count(ManifestState.partial);
    }

    @JsonProperty("removed")
    public int removed() {
        return states.count(ManifestState.removed);
    }

    public static final class Builder {
        DatasetRecord datasetRecord;
        TemporalUnit temporalUnit;
        String startLot;
        String endLot;
        long intervals;
        String firstLot;
        String lastLot;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withDatasetRecord(DatasetRecord datasetRecord) {
            this.datasetRecord = datasetRecord;
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

        public Builder withFirstLot(String firstLot) {
            this.firstLot = firstLot;
            return this;
        }

        public Builder withLastLot(String lastLot) {
            this.lastLot = lastLot;
            return this;
        }

        public DatasetStatusSummaryRecord build() {
            DatasetStatusSummaryRecord datasetStatusSummaryRecord = new DatasetStatusSummaryRecord();
            datasetStatusSummaryRecord.intervals = this.intervals;
            datasetStatusSummaryRecord.datasetRecord = this.datasetRecord;
            datasetStatusSummaryRecord.temporalUnit = this.temporalUnit;
            datasetStatusSummaryRecord.endLot = this.endLot;
            datasetStatusSummaryRecord.startLot = this.startLot;
            datasetStatusSummaryRecord.lastLot = this.lastLot;
            datasetStatusSummaryRecord.firstLot = this.firstLot;
            return datasetStatusSummaryRecord;
        }
    }
}

