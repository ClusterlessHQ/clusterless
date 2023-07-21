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
