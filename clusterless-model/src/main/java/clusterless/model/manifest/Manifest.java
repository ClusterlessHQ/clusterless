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
import clusterless.model.UriType;
import clusterless.model.deploy.Dataset;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;

/**
 *
 */
public class Manifest implements Content, Struct {
    public static final String JSON_EXTENSION = "json";
    @JsonProperty(required = true)
    Dataset dataset;

    @JsonProperty(required = true)
    String lotId;

    @JsonProperty(required = true)
    UriType uriType = UriType.prefix;
    @JsonProperty(required = true)
    List<URI> uris;

    public Manifest() {
    }

    private Manifest(Builder builder) {
        dataset = builder.dataset;
        lotId = builder.lotId;
        uriType = builder.uriType;
        uris = builder.uris;
    }

    public Dataset dataset() {
        return dataset;
    }

    public String lotId() {
        return lotId;
    }

    public UriType datasetURIType() {
        return uriType;
    }

    public List<URI> uris() {
        return uris;
    }

    @Override
    public String extension() {
        return JSON_EXTENSION;
    }

    @Override
    public String contentType() {
        return "application/json";
    }


    /**
     * {@code Manifest} builder static inner class.
     */
    public static final class Builder {
        private Dataset dataset;
        private String lotId;
        private UriType uriType;
        private List<URI> uris;

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
         * @param val the {@code lot} to set
         * @return a reference to this Builder
         */
        public Builder withLotId(String val) {
            lotId = val;
            return this;
        }

        /**
         * Sets the {@code uriType} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code uriType} to set
         * @return a reference to this Builder
         */
        public Builder withUriType(UriType val) {
            uriType = val;
            return this;
        }

        /**
         * Sets the {@code uris} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code uris} to set
         * @return a reference to this Builder
         */
        public Builder withUris(List<URI> val) {
            uris = val;
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
}
