/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.model.Struct;

import java.net.URI;
import java.util.Map;

/**
 *
 */
public class ArcProps implements Struct {
    Map<String, URI> sourceManifests;

    Map<String, URI> sinkManifests;

    public ArcProps() {
    }

    private ArcProps(Builder builder) {
        sourceManifests = builder.sourceManifests;
        sinkManifests = builder.sinkManifests;
    }


    /**
     * {@code ArcProps} builder static inner class.
     */
    public static final class Builder {
        private Map<String, URI> sourceManifests;
        private Map<String, URI> sinkManifests;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code sourceManifests} and returns a reference to this Builder enabling method chaining.
         *
         * @param sourceManifests the {@code sourceManifests} to set
         * @return a reference to this Builder
         */
        public Builder withSourceManifests(Map<String, URI> sourceManifests) {
            this.sourceManifests = sourceManifests;
            return this;
        }

        /**
         * Sets the {@code sinkManifests} and returns a reference to this Builder enabling method chaining.
         *
         * @param sinkManifests the {@code sinkManifests} to set
         * @return a reference to this Builder
         */
        public Builder withSinkManifests(Map<String, URI> sinkManifests) {
            this.sinkManifests = sinkManifests;
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
