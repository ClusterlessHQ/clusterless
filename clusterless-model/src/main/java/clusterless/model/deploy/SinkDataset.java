/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.managed.component.DocumentsModel;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.net.URI;

/**
 *
 */
@DocumentsModel(
        synopsis = "The sink dataset descriptor.",
        description = """
                Where the outgoing data will be located.
                  
                This dataset does not need to exist, this arc is responsible for creating it.
                                
                name: The name of the dataset. Required.
                                
                version: The version of the dataset. Required.
                         It's recommended to use a date for the version, such as "20230101".
                         
                pathURI: The prefix URI of the dataset. Required.
                         The URI must be a valid URI and must be accessible. This is typically a S3 URI.
                """
)
public class SinkDataset extends Dataset {

    @JsonIgnore
    boolean publish = true;

    public SinkDataset() {
    }

    public boolean publish() {
        return publish;
    }

    public static final class Builder {
        String name;
        String version;
        URI pathURI;
        boolean publish = true;

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

        public Builder withPublish(boolean publish) {
            this.publish = publish;
            return this;
        }

        public SinkDataset build() {
            SinkDataset sinkDataset = new SinkDataset();
            sinkDataset.publish = this.publish;
            sinkDataset.version = this.version;
            sinkDataset.name = this.name;
            sinkDataset.pathURI = this.pathURI;
            return sinkDataset;
        }
    }
}
