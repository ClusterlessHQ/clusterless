/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.manifest;

import clusterless.model.Content;
import clusterless.model.Struct;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class Manifest implements Content, Struct {
    @JsonProperty(required = true)
    String datasetName;
    String datasetVersion;
    @JsonProperty(required = true)
    String lot;
    @JsonProperty(required = true)
    URI datasetPrefix;
    @JsonProperty(required = true)
    List<String> datasetItems;

    public Manifest() {
    }

    private Manifest(Builder builder) {
        datasetName = builder.datasetName;
        datasetVersion = builder.datasetVersion;
        lot = builder.lot;
        datasetPrefix = builder.datasetPrefix;
        datasetItems = builder.datasetItems;
    }

    public String datasetName() {
        return datasetName;
    }

    public String datasetVersion() {
        return datasetVersion;
    }

    public String lot() {
        return lot;
    }

    public URI datasetPrefix() {
        return datasetPrefix;
    }

    public List<String> datasetItems() {
        return datasetItems;
    }

    @Override
    public String extension() {
        return "json";
    }

    @Override
    public String contentType() {
        return "application/json";
    }

    /**
     * {@code Manifest} builder static inner class.
     */
    public static final class Builder {
        private String datasetName;
        private String datasetVersion;
        private String lot;
        private URI datasetPrefix;
        private List<String> datasetItems = new LinkedList<>();

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
         * Sets the {@code lot} and returns a reference to this Builder enabling method chaining.
         *
         * @param lot the {@code lot} to set
         * @return a reference to this Builder
         */
        public Builder withLot(String lot) {
            this.lot = lot;
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
         * Sets the {@code datasetItems} and returns a reference to this Builder enabling method chaining.
         *
         * @param datasetItems the {@code datasetItems} to set
         * @return a reference to this Builder
         */
        public Builder withDatasetItems(List<String> datasetItems) {
            this.datasetItems = datasetItems;
            return this;
        }

        /**
         * Returns a {@code Manifest} built from the parameters previously set.
         *
         * @return a {@code Manifest} built with parameters of this {@code Manifest.Builder}
         */
        public Manifest build() {
            return new Manifest(this);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Manifest{");
        sb.append("datasetName='").append(datasetName).append('\'');
        sb.append(", datasetVersion='").append(datasetVersion).append('\'');
        sb.append(", lot='").append(lot).append('\'');
        sb.append(", datasetPrefix=").append(datasetPrefix);
        sb.append(", datasetItems=").append(datasetItems);
        sb.append('}');
        return sb.toString();
    }
}
