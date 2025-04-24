/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.runtime;

import clusterless.cls.model.Struct;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Project;

/**
 * The ArcMeta is meta-data about the running arc so that it can be managed (executed, reported on, etc) by tools.
 */
public class ArcMeta implements Struct {
    Project project = new Project();
    Placement placement = new Placement();
    Arc<?> arc = new Arc<>();
    ArcDeployment arcDeployment = new ArcDeployment();

    public ArcMeta() {
    }

    private ArcMeta(Builder builder) {
        project = builder.project;
        placement = builder.placement;
        arc = builder.arc;
        arcDeployment = builder.arcDeployment;
    }

    public Project project() {
        return project;
    }

    public Placement placement() {
        return placement;
    }

    public Arc<?> arc() {
        return arc;
    }

    public ArcDeployment arcDeployment() {
        return arcDeployment;
    }

    /**
     * {@code ArcMeta} builder static inner class.
     */
    public static final class Builder {
        private Project project;
        private Placement placement;
        private Arc arc;
        private ArcDeployment arcDeployment;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code project} and returns a reference to this Builder enabling method chaining.
         *
         * @param project the {@code project} to set
         * @return a reference to this Builder
         */
        public Builder withProject(Project project) {
            this.project = project;
            return this;
        }

        /**
         * Sets the {@code placement} and returns a reference to this Builder enabling method chaining.
         *
         * @param placement the {@code placement} to set
         * @return a reference to this Builder
         */
        public Builder withPlacement(Placement placement) {
            this.placement = placement;
            return this;
        }

        /**
         * Sets the {@code arc} and returns a reference to this Builder enabling method chaining.
         *
         * @param arc the {@code arc} to set
         * @return a reference to this Builder
         */
        public Builder withArc(Arc arc) {
            this.arc = arc;
            return this;
        }

        /**
         * Sets the {@code arcDeployment} and returns a reference to this Builder enabling method chaining.
         *
         * @param arcDeployment the {@code arcDeployment} to set
         * @return a reference to this Builder
         */
        public Builder withArcDeployment(ArcDeployment arcDeployment) {
            this.arcDeployment = arcDeployment;
            return this;
        }

        /**
         * Returns a {@code ArcMeta} built from the parameters previously set.
         *
         * @return a {@code ArcMeta} built with parameters of this {@code ArcMeta.Builder}
         */
        public ArcMeta build() {
            return new ArcMeta(this);
        }
    }
}
