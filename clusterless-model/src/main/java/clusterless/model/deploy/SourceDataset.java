/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.managed.component.DocumentsModel;

import java.net.URI;

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

    public boolean subscribe() {
        return subscribe;
    }

    public static final class Builder {
        String name;
        String version;
        URI pathURI;
        boolean subscribe = true;

        private Builder() {
        }

        public static Builder builder() {
            return new SourceDataset.Builder();
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

        public Builder withSubscribe(boolean subscribe) {
            this.subscribe = subscribe;
            return this;
        }

        public SourceDataset build() {
            SourceDataset sourceDataset = new SourceDataset();
            sourceDataset.version = this.version;
            sourceDataset.name = this.name;
            sourceDataset.subscribe = this.subscribe;
            sourceDataset.pathURI = this.pathURI;
            return sourceDataset;
        }
    }
}
