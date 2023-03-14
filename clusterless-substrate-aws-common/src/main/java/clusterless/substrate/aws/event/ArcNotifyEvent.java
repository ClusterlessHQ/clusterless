/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.event;

import clusterless.model.Struct;
import clusterless.model.deploy.Dataset;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

/**
 *
 */

/**
 * The ArcNotifyEvent is passed between Arc state machines.
 * <p>
 * This is the JSON observed on and subscribed to on an event bus.
 */
public class ArcNotifyEvent implements NotifyEvent, Struct {
    public static final String SOURCE = "clusterless.arc";
    public static final String DETAIL = "Clusterless Arc Notification";

    public static final String DATASET_ID = "datasetId";

    Dataset dataset;
    String lotId;
    URI manifest;

    public ArcNotifyEvent() {
    }

    private ArcNotifyEvent(Builder builder) {
        dataset = builder.dataset;
        lotId = builder.lotId;
        manifest = builder.manifest;
    }

    @Override
    public String eventSource() {
        return SOURCE;
    }

    @Override
    public String eventDetail() {
        return DETAIL;
    }

    public Dataset dataset() {
        return dataset;
    }

    public String lotId() {
        return lotId;
    }

    public URI manifest() {
        return manifest;
    }

    /**
     * This is a fabricated value for use with pattern matching.
     * <p>
     * We are attempting to bypass any complexity if a listener is listening for two or more datasets.
     *
     * @return a single string for pattern matching
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String datasetId() {
        return createDatasetId(dataset.name(), dataset.version());
    }

    public static String createDatasetId(String datasetName, String datasetVersion) {
        return String.format("%s/%s", datasetName, datasetVersion);
    }

    /**
     * {@code ArcNotifyEvent} builder static inner class.
     */
    public static final class Builder {
        private Dataset dataset;
        private String lotId;
        private URI manifest;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code dataset} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code dataset} to set
         * @return a reference to this Builder
         */
        public Builder withDataset(Dataset val) {
            dataset = val;
            return this;
        }

        /**
         * Sets the {@code lotId} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code lotId} to set
         * @return a reference to this Builder
         */
        public Builder withLotId(String val) {
            lotId = val;
            return this;
        }

        /**
         * Sets the {@code manifest} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code manifest} to set
         * @return a reference to this Builder
         */
        public Builder withManifest(URI val) {
            manifest = val;
            return this;
        }

        /**
         * Returns a {@code ArcNotifyEvent} built from the parameters previously set.
         *
         * @return a {@code ArcNotifyEvent} built with parameters of this {@code ArcNotifyEvent.Builder}
         */
        public ArcNotifyEvent build() {
            return new ArcNotifyEvent(this);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ArcNotifyEvent{");
        sb.append("dataset=").append(dataset);
        sb.append(", lotId='").append(lotId).append('\'');
        sb.append(", manifest=").append(manifest);
        sb.append('}');
        return sb.toString();
    }
}
