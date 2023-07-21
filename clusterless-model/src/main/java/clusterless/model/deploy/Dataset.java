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

    public static Builder builder() {
        return Builder.builder();
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

    public static final class Builder {
        String name;
        String version;
        URI pathURI;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder withPathURI(URI pathURI) {
            this.pathURI = pathURI;
            return this;
        }

        public Dataset build() {
            Dataset dataset = new Dataset();
            dataset.version = this.version;
            dataset.name = this.name;
            dataset.pathURI = this.pathURI;
            return dataset;
        }
    }
}
