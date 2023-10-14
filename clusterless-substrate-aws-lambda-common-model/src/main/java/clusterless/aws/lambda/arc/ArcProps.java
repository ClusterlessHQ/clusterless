/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.arc;

import clusterless.cls.model.Struct;
import clusterless.cls.model.deploy.SinkDataset;
import clusterless.cls.model.deploy.SourceDataset;
import clusterless.cls.model.deploy.WorkloadProps;
import clusterless.cls.substrate.uri.ManifestURI;

import java.util.Map;

/**
 *
 */
public class ArcProps<P extends WorkloadProps> implements Struct {
    Map<String, SourceDataset> sources;
    Map<String, SinkDataset> sinks;

    Map<String, ManifestURI> sourceManifestPaths;
    Map<String, ManifestURI> sinkManifestTemplates;

    P workloadProps;

    public ArcProps() {
    }

    public static <P extends WorkloadProps> Builder<P> builder() {
        return Builder.builder();
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

    public Map<String, ManifestURI> sinkManifestTemplates() {
        return sinkManifestTemplates;
    }

    public P workloadProps() {
        return workloadProps;
    }

    public static final class Builder<P extends WorkloadProps> {
        Map<String, SourceDataset> sources;
        Map<String, SinkDataset> sinks;
        Map<String, ManifestURI> sourceManifestPaths;
        Map<String, ManifestURI> sinkManifestTemplates;
        P workloadProps;

        private Builder() {
        }

        public static <P extends WorkloadProps> Builder<P> builder() {
            return new Builder<P>();
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

        public Builder<P> withSinkManifestTemplates(Map<String, ManifestURI> sinkManifestTemplates) {
            this.sinkManifestTemplates = sinkManifestTemplates;
            return this;
        }

        public Builder<P> withWorkloadProps(P workloadProps) {
            this.workloadProps = workloadProps;
            return this;
        }

        public ArcProps<P> build() {
            ArcProps<P> arcProps = new ArcProps<P>();
            arcProps.sinkManifestTemplates = this.sinkManifestTemplates;
            arcProps.sinks = this.sinks;
            arcProps.sources = this.sources;
            arcProps.workloadProps = this.workloadProps;
            arcProps.sourceManifestPaths = this.sourceManifestPaths;
            return arcProps;
        }
    }
}
