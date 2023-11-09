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
        "earliest",
        "earliestFound",
        "earliestGap",
        "latest",
        "latestFound",
        "latestGap",
        "removed",
        "partial",
        "empty",
        "complete",
        "totalFound",
        "range",
        "rangeGap"
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

        public Builder withDatasetRecord(DatasetRecord datasetRecord) {
            this.datasetRecord = datasetRecord;
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

        public DatasetStatusSummaryRecord build() {
            DatasetStatusSummaryRecord datasetStatusSummaryRecord = new DatasetStatusSummaryRecord();
            datasetStatusSummaryRecord.earliestLot = this.earliestLot;
            datasetStatusSummaryRecord.datasetRecord = this.datasetRecord;
            datasetStatusSummaryRecord.latestLot = this.latestLot;
            datasetStatusSummaryRecord.lastFoundLot = this.lastFoundLot;
            datasetStatusSummaryRecord.firstFoundLot = this.firstFoundLot;
            datasetStatusSummaryRecord.rangeIntervals = this.rangeIntervals;
            datasetStatusSummaryRecord.temporalUnit = this.temporalUnit;
            return datasetStatusSummaryRecord;
        }
    }
}
