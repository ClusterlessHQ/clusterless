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
public class SinkDataset extends Dataset {

    boolean publish = true;

    public SinkDataset() {
    }

    private SinkDataset(Builder builder) {
        name = builder.name;
        version = builder.version;
        pathURI = builder.pathURI;
        publish = builder.publish;
    }

    public boolean publish() {
        return publish;
    }


    /**
     * {@code SinkDataset} builder static inner class.
     */
    public static final class Builder {
        private String name;
        private String version;
        private URI pathURI;
        private boolean publish;

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
         * Sets the {@code pathURI} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code pathURI} to set
         * @return a reference to this Builder
         */
        public Builder withPathURI(URI val) {
            pathURI = val;
            return this;
        }

        /**
         * Sets the {@code publish} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code publish} to set
         * @return a reference to this Builder
         */
        public Builder withPublish(boolean val) {
            publish = val;
            return this;
        }

        /**
         * Returns a {@code SinkDataset} built from the parameters previously set.
         *
         * @return a {@code SinkDataset} built with parameters of this {@code SinkDataset.Builder}
         */
        public SinkDataset build() {
            return new SinkDataset(this);
        }
    }
}
