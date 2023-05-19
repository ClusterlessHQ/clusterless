/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.event;

import clusterless.model.Struct;
import clusterless.model.state.ArcState;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The ArcExecContext is the object passed within a state machine.
 */
public class ArcStateContext implements Struct {
    public static final String ERROR_PATH = "$.workloadError";
    public static final String RESPONSE_PATH = "$.workloadResponse";
    public static final String SINK_MANIFESTS_PATH = "$.sinkManifests";
    public static final String WORKLOAD_CONTEXT_PATH = "$.arcWorkloadContext";

    ArcState previousState;
    ArcState currentState;
    /**
     * Passed to all workloads
     */
    ArcWorkloadContext arcWorkloadContext = new ArcWorkloadContext();
    Map<String, URI> sinkManifests = new LinkedHashMap<>(); // result of the arc workload
    Map<String, Object> workloadResponse = new LinkedHashMap<>(); // intermediate results of workload
    Map<String, Object> workloadError = new LinkedHashMap<>(); // error results of workload

    public ArcStateContext() {
    }

    public static Builder builder() {
        return Builder.builder();
    }

    public ArcState previousState() {
        return previousState;
    }

    public ArcState currentState() {
        return currentState;
    }

    public ArcWorkloadContext arcWorkloadContext() {
        return arcWorkloadContext;
    }

    public Map<String, URI> sinkManifests() {
        return sinkManifests;
    }

    public Map<String, Object> workloadResponse() {
        return workloadResponse;
    }

    public Map<String, Object> workloadError() {
        return workloadError;
    }

    public static final class Builder {
        ArcState previousState;
        ArcState currentState;
        ArcWorkloadContext arcWorkloadContext = new ArcWorkloadContext();
        Map<String, URI> sinkManifests = new LinkedHashMap<>(); // result of the arc workload
        Map<String, Object> workloadResponse = new LinkedHashMap<>(); // intermediate results of workload
        Map<String, Object> workloadError = new LinkedHashMap<>(); // error results of workload

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withPreviousState(ArcState previousState) {
            this.previousState = previousState;
            return this;
        }

        public Builder withCurrentState(ArcState currentState) {
            this.currentState = currentState;
            return this;
        }

        public Builder withArcWorkloadContext(ArcWorkloadContext arcWorkloadContext) {
            this.arcWorkloadContext = arcWorkloadContext;
            return this;
        }

        public Builder withSinkManifests(Map<String, URI> sinkManifests) {
            this.sinkManifests = sinkManifests;
            return this;
        }

        public Builder withWorkloadResponse(Map<String, Object> workloadResponse) {
            this.workloadResponse = workloadResponse;
            return this;
        }

        public Builder withWorkloadError(Map<String, Object> workloadError) {
            this.workloadError = workloadError;
            return this;
        }

        public ArcStateContext build() {
            ArcStateContext arcStateContext = new ArcStateContext();
            arcStateContext.workloadError = this.workloadError;
            arcStateContext.arcWorkloadContext = this.arcWorkloadContext;
            arcStateContext.workloadResponse = this.workloadResponse;
            arcStateContext.currentState = this.currentState;
            arcStateContext.previousState = this.previousState;
            arcStateContext.sinkManifests = this.sinkManifests;
            return arcStateContext;
        }
    }
}
