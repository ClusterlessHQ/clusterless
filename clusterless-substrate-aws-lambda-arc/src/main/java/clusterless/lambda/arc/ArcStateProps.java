/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.model.Struct;
import clusterless.model.deploy.Project;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.deploy.SourceDataset;
import clusterless.substrate.aws.uri.ArcURI;

import java.util.Map;

public class ArcStateProps implements Struct {
    Project project;
    String name;
    Map<String, SourceDataset> sources;
    Map<String, SinkDataset> sinks;
    ArcURI arcStatePath;
    String eventBusName;

    public static Builder builder() {
        return Builder.anArcStateProps();
    }

    public Project project() {
        return project;
    }

    public String name() {
        return name;
    }

    public Map<String, SourceDataset> sources() {
        return sources;
    }

    public Map<String, SinkDataset> sinks() {
        return sinks;
    }

    public ArcURI arcStatePath() {
        return arcStatePath;
    }

    public String eventBusName() {
        return eventBusName;
    }

    public static final class Builder {
        Project project;
        String name;
        Map<String, SourceDataset> sources;
        Map<String, SinkDataset> sinks;
        ArcURI arcStatePath;
        String eventBusName;

        private Builder() {
        }

        public static Builder anArcStateProps() {
            return new Builder();
        }

        public Builder withProject(Project project) {
            this.project = project;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withSources(Map<String, SourceDataset> sources) {
            this.sources = sources;
            return this;
        }

        public Builder withSinks(Map<String, SinkDataset> sinks) {
            this.sinks = sinks;
            return this;
        }

        public Builder withArcStatePath(ArcURI arcStatePath) {
            this.arcStatePath = arcStatePath;
            return this;
        }

        public Builder withEventBusName(String eventBusName) {
            this.eventBusName = eventBusName;
            return this;
        }

        public ArcStateProps build() {
            ArcStateProps arcStateProps = new ArcStateProps();
            arcStateProps.project = this.project;
            arcStateProps.eventBusName = this.eventBusName;
            arcStateProps.sinks = this.sinks;
            arcStateProps.sources = this.sources;
            arcStateProps.name = this.name;
            arcStateProps.arcStatePath = this.arcStatePath;
            return arcStateProps;
        }
    }
}
