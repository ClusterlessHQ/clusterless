/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.model.Struct;
import clusterless.model.deploy.Dataset;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

/**
 *
 */
public class TransformProps implements Struct {
    @JsonProperty(required = true)
    String lotUnit;

    @JsonProperty(required = true)
    LotSource lotSource;

    String keyRegex;

    @JsonProperty(required = true)
    URI manifestPath;

    @JsonProperty(required = true)
    Dataset dataset;

    String eventBusName;

    public TransformProps() {
    }

    private TransformProps(Builder builder) {
        lotUnit = builder.lotUnit;
        lotSource = builder.lotSource;
        keyRegex = builder.keyRegex;
        manifestPath = builder.manifestPath;
        dataset = builder.dataset;
        eventBusName = builder.eventBusName;
    }

    public String lotUnit() {
        return lotUnit;
    }

    public LotSource lotSource() {
        return lotSource;
    }

    public String keyRegex() {
        return keyRegex;
    }

    public URI manifestPath() {
        return manifestPath;
    }


    public Dataset dataset() {
        return dataset;
    }

    public String eventBusName() {
        return eventBusName;
    }

    /**
     * {@code TransformProps} builder static inner class.
     */
    public static final class Builder {
        private String lotUnit;
        private LotSource lotSource;
        private String keyRegex;
        private URI manifestPath;
        private Dataset dataset;
        private String eventBusName;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code lotUnit} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code lotUnit} to set
         * @return a reference to this Builder
         */
        public Builder withLotUnit(String val) {
            lotUnit = val;
            return this;
        }

        /**
         * Sets the {@code lotSource} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code lotSource} to set
         * @return a reference to this Builder
         */
        public Builder withLotSource(LotSource val) {
            lotSource = val;
            return this;
        }

        /**
         * Sets the {@code keyRegex} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code keyRegex} to set
         * @return a reference to this Builder
         */
        public Builder withKeyRegex(String val) {
            keyRegex = val;
            return this;
        }

        /**
         * Sets the {@code withManifestPath} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code manifestPrefix} to set
         * @return a reference to this Builder
         */
        public Builder withManifestPath(URI val) {
            manifestPath = val;
            return this;
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
         * Sets the {@code eventBusName} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code eventBusName} to set
         * @return a reference to this Builder
         */
        public Builder withEventBusName(String val) {
            eventBusName = val;
            return this;
        }

        /**
         * Returns a {@code TransformProps} built from the parameters previously set.
         *
         * @return a {@code TransformProps} built with parameters of this {@code TransformProps.Builder}
         */
        public TransformProps build() {
            return new TransformProps(this);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransformProps{");
        sb.append("lotUnit='").append(lotUnit).append('\'');
        sb.append(", lotSource=").append(lotSource);
        sb.append(", keyRegex='").append(keyRegex).append('\'');
        sb.append(", manifestPath=").append(manifestPath);
        sb.append(", dataset=").append(dataset);
        sb.append(", eventBusName='").append(eventBusName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
