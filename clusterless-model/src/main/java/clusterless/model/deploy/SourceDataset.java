/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import java.net.URI;

/**
 *
 */
public class SourceDataset extends Dataset {

    boolean subscribe = true;

    private SourceDataset(Builder builder) {
        name = builder.name;
        version = builder.version;
        locationURI = builder.locationURI;
        subscribe = builder.subscribe;
    }

    public boolean subscribe() {
        return subscribe;
    }


    /**
     * {@code SourceDataset} builder static inner class.
     */
    public static final class Builder {
        private String name;
        private String version;
        private URI locationURI;
        private boolean subscribe;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code name} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code name} to set
         * @return a reference to this Builder
         */
        public Builder withName(String val) {
            name = val;
            return this;
        }

        /**
         * Sets the {@code version} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code version} to set
         * @return a reference to this Builder
         */
        public Builder withVersion(String val) {
            version = val;
            return this;
        }

        /**
         * Sets the {@code locationURI} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code locationURI} to set
         * @return a reference to this Builder
         */
        public Builder withLocationURI(URI val) {
            locationURI = val;
            return this;
        }

        /**
         * Sets the {@code subscribe} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code subscribe} to set
         * @return a reference to this Builder
         */
        public Builder withSubscribe(boolean val) {
            subscribe = val;
            return this;
        }

        /**
         * Returns a {@code SourceDataset} built from the parameters previously set.
         *
         * @return a {@code SourceDataset} built with parameters of this {@code SourceDataset.Builder}
         */
        public SourceDataset build() {
            return new SourceDataset(this);
        }
    }
}
