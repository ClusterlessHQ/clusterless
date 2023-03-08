/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.model.Model;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

/**
 *
 */
public class Dataset extends Model {
    @JsonProperty(required = true)
    String name;
    @JsonProperty(required = true)
    String version;
    @JsonProperty(required = true)
    URI pathURI;

    protected Dataset() {
    }

    public Dataset(Dataset other) {
        this.name = other.name;
        this.version = other.version;
        this.pathURI = other.pathURI;
    }

    private Dataset(Builder builder) {
        name = builder.name;
        version = builder.version;
        pathURI = builder.pathURI;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public URI pathURI() {
        return pathURI;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Dataset{");
        sb.append("name='").append(name).append('\'');
        sb.append(", version='").append(version).append('\'');
        sb.append(", pathURI=").append(pathURI);
        sb.append('}');
        return sb.toString();
    }

    /**
     * {@code Dataset} builder static inner class.
     */
    public static final class Builder {
        private String name;
        private String version;
        private URI pathURI;

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
         * Returns a {@code Dataset} built from the parameters previously set.
         *
         * @return a {@code Dataset} built with parameters of this {@code Dataset.Builder}
         */
        public Dataset build() {
            return new Dataset(this);
        }
    }
}
