/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.model.Struct;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.deploy.SourceDataset;
import clusterless.substrate.aws.uri.ManifestURI;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class ArcProps implements Struct {
    Map<String, SourceDataset> sources;
    Map<String, SinkDataset> sinks;

    Map<String, ManifestURI> sourceManifestPaths;
    Map<String, ManifestURI> sinkManifestCompletePaths;
    Map<String, ManifestURI> sinkManifestPartialPaths;

    public ArcProps() {
    }

    public Map<String, SourceDataset> sources() {
        return sources;
    }

    public Map<String, SinkDataset> sinks() {
        return sinks;
    }

    public Map<String, ManifestURI> sourceManifestPaths() {
        return sourceManifestPaths;
    }

    public Map<String, ManifestURI> sinkManifestCompletePaths() {
        return sinkManifestCompletePaths;
    }

    public Map<String, ManifestURI> sinkManifestPartialPaths() {
        return sinkManifestPartialPaths;
    }

    private ArcProps(Builder builder) {
        sources = builder.sources;
        sinks = builder.sinks;
        sourceManifestPaths = builder.sourceManifestPaths;
        sinkManifestCompletePaths = builder.sinkManifestCompletePaths;
        sinkManifestPartialPaths = builder.sinkManifestPartialPaths;
    }

    /**
     * {@code ArcProps} builder static inner class.
     */
    public static final class Builder {
        private Map<String, SourceDataset> sources = new LinkedHashMap<>();
        private Map<String, SinkDataset> sinks = new LinkedHashMap<>();
        private Map<String, ManifestURI> sourceManifestPaths;
        private Map<String, ManifestURI> sinkManifestCompletePaths;
        private Map<String, ManifestURI> sinkManifestPartialPaths;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code sources} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code sources} to set
         * @return a reference to this Builder
         */
        public Builder withSources(Map<String, SourceDataset> val) {
            sources = val;
            return this;
        }

        /**
         * Sets the {@code sinks} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code sinks} to set
         * @return a reference to this Builder
         */
        public Builder withSinks(Map<String, SinkDataset> val) {
            sinks = val;
            return this;
        }

        /**
         * Sets the {@code sourceManifestPaths} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code sourceManifestPaths} to set
         * @return a reference to this Builder
         */
        public Builder withSourceManifestPaths(Map<String, ManifestURI> val) {
            sourceManifestPaths = val;
            return this;
        }

        /**
         * Sets the {@code sinkManifestCompletePaths} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code sinkManifestCompletePaths} to set
         * @return a reference to this Builder
         */
        public Builder withSinkManifestCompletePaths(Map<String, ManifestURI> val) {
            sinkManifestCompletePaths = val;
            return this;
        }

        /**
         * Sets the {@code sinkManifestRollbackPaths} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code sinkManifestRollbackPaths} to set
         * @return a reference to this Builder
         */
        public Builder withSinkManifestPartialPaths(Map<String, ManifestURI> val) {
            sinkManifestPartialPaths = val;
            return this;
        }

        /**
         * Returns a {@code ArcProps} built from the parameters previously set.
         *
         * @return a {@code ArcProps} built with parameters of this {@code ArcProps.Builder}
         */
        public ArcProps build() {
            return new ArcProps(this);
        }
    }
}