/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.model.Struct;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.deploy.SourceDataset;
import clusterless.substrate.aws.uri.ManifestURI;

import java.util.Map;

/**
 *
 */
public class ArcProps implements Struct {
    Map<String, SourceDataset> sources;
    Map<String, SinkDataset> sinks;

    Map<String, ManifestURI> sourceManifestPaths;
    Map<String, ManifestURI> sinkManifestPaths;

    public ArcProps() {
    }

    public static Builder builder() {
        return Builder.anArcProps();
    }

    public Map<String, SourceDataset> sources() {
        return sources;
    }

    public Map<String, SinkDataset> sinks() {
        return sinks;
    }

    public Map<String, ManifestURI> sourceManifestPaths() {
        return sourceManifestPaths;
    }

    public Map<String, ManifestURI> sinkManifestPaths() {
        return sinkManifestPaths;
    }

    public static final class Builder {
        Map<String, SourceDataset> sources;
        Map<String, SinkDataset> sinks;
        Map<String, ManifestURI> sourceManifestPaths;
        Map<String, ManifestURI> sinkManifestPaths;

        private Builder() {
        }

        public static Builder anArcProps() {
            return new Builder();
        }

        public Builder withSources(Map<String, SourceDataset> sources) {
            this.sources = sources;
            return this;
        }

        public Builder withSinks(Map<String, SinkDataset> sinks) {
            this.sinks = sinks;
            return this;
        }

        public Builder withSourceManifestPaths(Map<String, ManifestURI> sourceManifestPaths) {
            this.sourceManifestPaths = sourceManifestPaths;
            return this;
        }

        public Builder withSinkManifestPaths(Map<String, ManifestURI> sinkManifestPaths) {
            this.sinkManifestPaths = sinkManifestPaths;
            return this;
        }

        public ArcProps build() {
            ArcProps arcProps = new ArcProps();
            arcProps.sinks = this.sinks;
            arcProps.sourceManifestPaths = this.sourceManifestPaths;
            arcProps.sources = this.sources;
            arcProps.sinkManifestPaths = this.sinkManifestPaths;
            return arcProps;
        }
    }
}