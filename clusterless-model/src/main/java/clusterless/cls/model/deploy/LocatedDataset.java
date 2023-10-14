/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.model.deploy;

import clusterless.cls.json.JsonRequiredProperty;

import java.net.URI;

/**
 *
 */
public class LocatedDataset extends Dataset {
    @JsonRequiredProperty
    URI pathURI;

    protected LocatedDataset() {
    }

    public LocatedDataset(LocatedDataset other) {
        super(other);
        this.pathURI = other.pathURI;
    }

    private LocatedDataset(Builder builder) {
        name = builder.name;
        version = builder.version;
        pathURI = builder.pathURI;
    }

    public URI pathURI() {
        return pathURI;
    }

    public static final class Builder {
        URI pathURI;
        String name;
        String version;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withPathURI(URI pathURI) {
            this.pathURI = pathURI;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public LocatedDataset build() {
            LocatedDataset dataset = new LocatedDataset();
            dataset.name = this.name;
            dataset.pathURI = this.pathURI;
            dataset.version = this.version;
            return dataset;
        }
    }
}
