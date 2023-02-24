/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.event;

import clusterless.model.Struct;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

/**
 *
 */
public class ArcNotifyEvent implements NotifyEvent, Struct {
    public static final String SOURCE = "clusterless.arc";
    public static final String DETAIL = "Clusterless Arc Notification";

    public static final String DATASET_ID = "datasetId";

    String datasetName;
    String datasetVersion;
    String lotId;
    URI manifestURI;
    URI datasetPrefix;

    public ArcNotifyEvent() {
    }

    private ArcNotifyEvent(Builder builder) {
        datasetName = builder.datasetName;
        datasetVersion = builder.datasetVersion;
        lotId = builder.lotId;
        manifestURI = builder.manifestURI;
        datasetPrefix = builder.datasetPrefix;
    }

    public String datasetVersion() {
        return datasetVersion;
    }

    public String datasetName() {
        return datasetName;
    }

    public String lotId() {
        return lotId;
    }

    public URI manifestURI() {
        return manifestURI;
    }

    public URI datasetPrefix() {
        return datasetPrefix;
    }

    /**
     * This is a fabricated value for use with pattern matching.
     * <p>
     * We are attempting to bypass any complexity if a listener is listening for two or more datasets.
     *
     * @return a single string for pattern matching
     */
    @JsonProperty()
    public String datasetId() {
        return createDatasetId(datasetName(), datasetVersion());
    }

    public static String createDatasetId(String datasetName, String datasetVersion) {
        return String.format("%s/%s", datasetName, datasetVersion);
    }

    @Override
    public String eventSource() {
        return SOURCE;
    }

    @Override
    public String eventDetail() {
        return DETAIL;
    }

    /**
     * {@code ArcNotifyEvent} builder static inner class.
     */
    public static final class Builder {
        private String datasetName;
        private String datasetVersion;
        private String lotId;
        private URI manifestURI;
        private URI datasetPrefix;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code datasetName} and returns a reference to this Builder enabling method chaining.
         *
         * @param datasetName the {@code datasetName} to set
         * @return a reference to this Builder
         */
        public Builder withDatasetName(String datasetName) {
            this.datasetName = datasetName;
            return this;
        }

        /**
         * Sets the {@code datasetVersion} and returns a reference to this Builder enabling method chaining.
         *
         * @param datasetVersion the {@code datasetVersion} to set
         * @return a reference to this Builder
         */
        public Builder withDatasetVersion(String datasetVersion) {
            this.datasetVersion = datasetVersion;
            return this;
        }

        /**
         * Sets the {@code lotId} and returns a reference to this Builder enabling method chaining.
         *
         * @param lotId the {@code lotId} to set
         * @return a reference to this Builder
         */
        public Builder withLotId(String lotId) {
            this.lotId = lotId;
            return this;
        }

        /**
         * Sets the {@code manifestURI} and returns a reference to this Builder enabling method chaining.
         *
         * @param manifestURI the {@code manifestURI} to set
         * @return a reference to this Builder
         */
        public Builder withManifestURI(URI manifestURI) {
            this.manifestURI = manifestURI;
            return this;
        }

        /**
         * Sets the {@code datasetPrefix} and returns a reference to this Builder enabling method chaining.
         *
         * @param datasetPrefix the {@code datasetPrefix} to set
         * @return a reference to this Builder
         */
        public Builder withDatasetPrefix(URI datasetPrefix) {
            this.datasetPrefix = datasetPrefix;
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
}
