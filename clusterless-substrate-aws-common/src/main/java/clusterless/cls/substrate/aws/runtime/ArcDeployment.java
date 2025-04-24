/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.runtime;

import clusterless.cls.model.Struct;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ArcDeployment implements Struct {
    String stackName;
    String stepFunctionName;
    String listenerRuleName;
    Map<String, URI> manifestLocationURIs = new HashMap<>();

    public ArcDeployment() {
    }

    private ArcDeployment(Builder builder) {
        stackName = builder.stackName;
        stepFunctionName = builder.stepFunctionName;
        listenerRuleName = builder.listenerRuleName;
        manifestLocationURIs = builder.manifestLocationURIs;
    }

    public String stackName() {
        return stackName;
    }

    public String stepFunctionName() {
        return stepFunctionName;
    }

    public String listenerRuleName() {
        return listenerRuleName;
    }

    public Map<String, URI> manifestLocationURIs() {
        return manifestLocationURIs;
    }

    /**
     * {@code ArcDeployment} builder static inner class.
     */
    public static final class Builder {
        private String stackName;
        private String stepFunctionName;
        private String listenerRuleName;
        private Map<String, URI> manifestLocationURIs = new HashMap<>();

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
