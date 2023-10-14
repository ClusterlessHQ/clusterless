/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc;

import clusterless.cls.model.Struct;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Project;

import java.net.URI;
import java.util.Map;

/**
 * The ArcMeta is meta-data about the running arc so that it can be managed (executed, reported on, etc) by tools.
 */
public class ArcMeta implements Struct {

    public static class ArcDeployment {
        String stackName;
        String stepFunctionName;

        String listenerRuleName;
        Map<String, URI> manifestLocationURIs;

        private ArcDeployment(Builder builder) {
            stackName = builder.stackName;
            stepFunctionName = builder.stepFunctionName;
            listenerRuleName = builder.listenerRuleName;
            manifestLocationURIs = builder.manifestLocationURIs;
        }

        /**
         * {@code ArcDeployment} builder static inner class.
         */
        public static final class Builder {
            private String stackName;
            private String stepFunctionName;
            private String listenerRuleName;
            private Map<String, URI> manifestLocationURIs;

            private Builder() {
            }

            public static Builder builder() {
                return new Builder();
            }

            /**
             * Sets the {@code stackName} and returns a reference to this Builder enabling method chaining.
             *
             * @param stackName the {@code stackName} to set
             * @return a reference to this Builder
             */
            public Builder withStackName(String stackName) {
                this.stackName = stackName;
                return this;
            }

            /**
             * Sets the {@code stepFunctionName} and returns a reference to this Builder enabling method chaining.
             *
             * @param stepFunctionName the {@code stepFunctionName} to set
             * @return a reference to this Builder
             */
            public Builder withStepFunctionName(String stepFunctionName) {
                this.stepFunctionName = stepFunctionName;
                return this;
            }

            /**
             * Sets the {@code listenerRuleName} and returns a reference to this Builder enabling method chaining.
             *
             * @param listenerRuleName the {@code listenerRuleName} to set
             * @return a reference to this Builder
             */
            public Builder withListenerRuleName(String listenerRuleName) {
                this.listenerRuleName = listenerRuleName;
                return this;
            }

            /**
             * Sets the {@code manifestLocationURIs} and returns a reference to this Builder enabling method chaining.
             *
             * @param manifestLocationURIs the {@code manifestLocationURIs} to set
             * @return a reference to this Builder
             */
            public Builder withManifestLocationURIs(Map<String, URI> manifestLocationURIs) {
                this.manifestLocationURIs = manifestLocationURIs;
                return this;
            }

            /**
             * Returns a {@code ArcDeployment} built from the parameters previously set.
             *
             * @return a {@code ArcDeployment} built with parameters of this {@code ArcDeployment.Builder}
             */
            public ArcDeployment build() {
                return new ArcDeployment(this);
            }
        }
    }

    Project project;
    Placement placement;
    Arc arc;
    ArcDeployment arcDeployment;

    private ArcMeta(Builder builder) {
        project = builder.project;
        placement = builder.placement;
        arc = builder.arc;
        arcDeployment = builder.arcDeployment;
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
