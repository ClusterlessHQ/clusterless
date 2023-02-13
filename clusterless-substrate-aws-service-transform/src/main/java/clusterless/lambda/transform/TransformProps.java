/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.model.Struct;
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
    URI manifestPrefix;

    public TransformProps() {
    }

    public TransformProps(String lotUnit, LotSource lotSource, String keyRegex, URI manifestPrefix) {
        this.lotUnit = lotUnit;
        this.lotSource = lotSource;
        this.keyRegex = keyRegex;
        this.manifestPrefix = manifestPrefix;
    }

    private TransformProps(Builder builder) {
        lotUnit = builder.lotUnit;
        lotSource = builder.lotSource;
        keyRegex = builder.keyRegex;
        manifestPrefix = builder.manifestPrefix;
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

    public URI manifestPrefix() {
        return manifestPrefix;
    }

    /**
     * {@code Context} builder static inner class.
     */
    public static final class Builder {
        private String lotUnit;
        private LotSource lotSource;
        private String keyRegex;
        private URI manifestPrefix;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code lotUnit} and returns a reference to this Builder enabling method chaining.
         *
         * @param lotUnit the {@code lotUnit} to set
         * @return a reference to this Builder
         */
        public Builder withLotUnit(String lotUnit) {
            this.lotUnit = lotUnit;
            return this;
        }

        /**
         * Sets the {@code lotSource} and returns a reference to this Builder enabling method chaining.
         *
         * @param lotSource the {@code lotSource} to set
         * @return a reference to this Builder
         */
        public Builder withLotSource(LotSource lotSource) {
            this.lotSource = lotSource;
            return this;
        }

        /**
         * Sets the {@code keyRegex} and returns a reference to this Builder enabling method chaining.
         *
         * @param keyRegex the {@code keyRegex} to set
         * @return a reference to this Builder
         */
        public Builder withKeyRegex(String keyRegex) {
            this.keyRegex = keyRegex;
            return this;
        }

        /**
         * Sets the {@code manifestPrefix} and returns a reference to this Builder enabling method chaining.
         *
         * @param manifestPrefix the {@code manifestPrefix} to set
         * @return a reference to this Builder
         */
        public Builder withManifestPrefix(URI manifestPrefix) {
            this.manifestPrefix = manifestPrefix;
            return this;
        }

        /**
         * Returns a {@code Context} built from the parameters previously set.
         *
         * @return a {@code Context} built with parameters of this {@code Context.Builder}
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
        sb.append(", manifestPrefix=").append(manifestPrefix);
        sb.append('}');
        return sb.toString();
    }
}
