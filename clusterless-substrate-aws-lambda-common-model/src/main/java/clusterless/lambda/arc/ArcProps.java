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
import clusterless.model.deploy.WorkloadProps;
import clusterless.substrate.uri.ManifestURI;

import java.util.Map;

/**
 *
 */
public class ArcProps<P extends WorkloadProps> implements Struct {
    Map<String, SourceDataset> sources;
    Map<String, SinkDataset> sinks;

    Map<String, ManifestURI> sourceManifestPaths;
    Map<String, ManifestURI> sinkManifestPaths;

    P workloadProps;

    public ArcProps() {
    }

    public static <P extends WorkloadProps> Builder<P> builder() {
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

    public P workloadProps() {
        return workloadProps;
    }

    public static final class Builder<P extends WorkloadProps> {
        Map<String, SourceDataset> sources;
        Map<String, SinkDataset> sinks;
        Map<String, ManifestURI> sourceManifestPaths;
        Map<String, ManifestURI> sinkManifestPaths;
        P workloadProps;

        private Builder() {
        }

        private static <P extends WorkloadProps> Builder<P> anArcProps() {
            return new Builder<>();
        }

        public Builder<P> withSources(Map<String, SourceDataset> sources) {
            this.sources = sources;
            return this;
        }

        public Builder<P> withSinks(Map<String, SinkDataset> sinks) {
            this.sinks = sinks;
            return this;
        }

        public Builder<P> withSourceManifestPaths(Map<String, ManifestURI> sourceManifestPaths) {
            this.sourceManifestPaths = sourceManifestPaths;
            return this;
        }

        public Builder<P> withSinkManifestPaths(Map<String, ManifestURI> sinkManifestPaths) {
            this.sinkManifestPaths = sinkManifestPaths;
            return this;
        }

        public Builder<P> withWorkloadProps(P workloadProps) {
            this.workloadProps = workloadProps;
            return this;
        }

        public ArcProps<P> build() {
            ArcProps<P> arcProps = new ArcProps<>();
            arcProps.sources = this.sources;
            arcProps.sinks = this.sinks;
            arcProps.workloadProps = this.workloadProps;
            arcProps.sinkManifestPaths = this.sinkManifestPaths;
            arcProps.sourceManifestPaths = this.sourceManifestPaths;
            return arcProps;
        }
    }
}
