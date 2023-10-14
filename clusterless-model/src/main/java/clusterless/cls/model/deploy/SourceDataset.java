/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.model.deploy;

import clusterless.cls.managed.component.DocumentsModel;

import java.util.Objects;

/**
 *
 */
@DocumentsModel(
        synopsis = "The source dataset descriptor.",
        description = """
                Where the incoming data is located.
                                
                This dataset (name and version) must already exist, it should be produced by an upstream 
                IngressBoundary or Arc.
                                
                name: The name of the dataset. Required.
                                
                version: The version of the dataset. Required.
                         It's recommended to use a date for the version, such as "20230101".
                         
                pathURI: The prefix URI of the dataset. Required.
                         The URI must be a valid URI and must be accessible. This is typically a S3 URI.
                                
                subscribe: Whether the dataset should be subscribed to. Optional.
                           Defaults to true. Use this to disable receiving events from the dataset.
                """
)
public class SourceDataset extends Dataset {

    boolean subscribe = true;

    public SourceDataset() {
    }

    public SourceDataset(Dataset other) {
        super(other);
    }

    public boolean subscribe() {
        return subscribe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SourceDataset that = (SourceDataset) o;
        return subscribe == that.subscribe;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subscribe);
    }

    public static final class Builder {
        String name;
        String version;
        boolean subscribe = true;

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

        public Builder withSubscribe(boolean subscribe) {
            this.subscribe = subscribe;
            return this;
        }

        public SourceDataset build() {
            SourceDataset sourceDataset = new SourceDataset();
            sourceDataset.name = this.name;
            sourceDataset.version = this.version;
            sourceDataset.subscribe = this.subscribe;
            return sourceDataset;
        }
    }
}
