/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.lambda.StreamResultHandler;
import clusterless.model.manifest.ManifestState;
import clusterless.model.state.ArcState;
import clusterless.substrate.aws.event.ArcStateContext;
import clusterless.substrate.aws.event.ArcWorkloadContext;
import clusterless.substrate.uri.ManifestURI;
import clusterless.substrate.uri.StateURI;
import clusterless.util.Env;
import com.amazonaws.services.lambda.runtime.Context;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class ArcStateCompleteHandler extends StreamResultHandler<ArcStateContext, ArcStateContext> {
    protected static final ArcStateProps arcStateProps = Env.fromEnv(
            ArcStateProps.class,
            () -> ArcStateProps.builder()
                    .build()
    );

    protected ArcStateManager arcStateManager = new ArcStateManager(arcStateProps.arcStatePath());

    protected Map<String, ArcNotifyEventPublisher> eventPublishers = ArcNotifyEventPublisher.publishers(
            arcStateProps.eventBusName(),
            arcStateProps.sinks()
    );

    protected ArcStateCompleteHandler(ArcStateManager arcStateManager, Map<String, ArcNotifyEventPublisher> eventPublishers) {
        this();
        this.arcStateManager = arcStateManager;
        this.eventPublishers = eventPublishers;
    }

    public ArcStateCompleteHandler() {
        super(ArcStateContext.class, ArcStateContext.class);
    }

    protected ArcStateCompleteObserver observer() {
        return new ArcStateCompleteObserver() {
            @Override
            public void applySinkManifests(Map<String, URI> sinkStates) {

            }

            @Override
            public void applyWorkloadError(Map<String, Object> workloadError) {

            }
        };
    }

    @Override
    public ArcStateContext handleRequest(ArcStateContext event, Context context) {
        logInfoObject("incoming arc event: {}", event);

        ArcStateContext arcStateContext = handleEvent(event, context, observer());

        logInfoObject("outgoing arc context: {}", arcStateContext);

        return arcStateContext;
    }

    protected ArcStateContext handleEvent(ArcStateContext stateContext, Context context, ArcStateCompleteObserver eventObserver) {
        Map<String, Object> workloadError = stateContext.workloadError();

        boolean hasWorkloadError = false;
        if (workloadError != null && !workloadError.isEmpty()) {
            eventObserver.applyWorkloadError(workloadError);
            hasWorkloadError = true;
        }

        boolean hasSinkManifests = false;
        Map<String, URI> sinkManifests = stateContext.sinkManifests();
        Set<ManifestState> sinkStates = Collections.emptySet();
        if (sinkManifests != null && !sinkManifests.isEmpty()) {
            eventObserver.applySinkManifests(sinkManifests);
            hasSinkManifests = true;
            sinkStates = sinkManifests.values()
                    .stream()
                    .map(ManifestURI::parse)
                    .map(StateURI::state)
                    .collect(Collectors.toSet());
        }

        //- `running` - the workload is currently processing, prevents concurrent executions
        //- `complete` - the workload completed successfully, prevents duplicate executions
        //- `partial` - the workload failed and any artifacts should be removed/ignored, allows for
        //              retries via a new running state
        //- `missing` - if a partial is cleaned up, the arc state may be considered intentionally
        //              missing and eligible for a retry
        ArcState newArcState = null;

        if (!hasSinkManifests && !hasWorkloadError) {
            // no manifest was written by the workload, but it was successful
            logErrorAndThrow(IllegalStateException::new, "workload sink manifests is missing/empty");
        } else if (!hasSinkManifests) {
            logInfo("workload had no results due to failure, marking missing");
            newArcState = ArcState.missing;
        } else if (sinkStates.contains(ManifestState.partial)) {
            logInfo("workload had partial results, marking partial");
            newArcState = ArcState.partial;
        } else if (sinkStates.size() == 1 && sinkStates.contains(ManifestState.empty)) {
            logInfo("workload has empty results, marking complete");
            newArcState = ArcState.complete;
        } else if (sinkStates.contains(ManifestState.complete)) {
            logInfo("workload has complete results");
            newArcState = ArcState.complete;
        }

        if (newArcState == null) {
            return logErrorAndThrow(IllegalStateException::new, "unexpected result states: {}", sinkManifests);
        }

        String lotId = stateContext.arcWorkloadContext().arcNotifyEvent().lot();

        if (newArcState == ArcState.complete) {
            // push notify on each sink
            for (Map.Entry<String, URI> entry : sinkManifests.entrySet()) {
                String role = entry.getKey();
                URI manifestURI = entry.getValue();

                // TODO: add status of arc
                eventPublishers.get(role).publishEvent(lotId, manifestURI);
            }
        }

        // change state to complete/partial
        Optional<ArcState> priorArcState = arcStateManager.setStateFor(lotId, newArcState, hasWorkloadError ? workloadError : null);

        // confirm there isn't some race condition
        // todo: create new exception to capture in state machine
        // this should always be running state
        ArcState currentState = stateContext.currentState();
        if (currentState == ArcState.running && currentState != priorArcState.orElse(null)) {
            return logErrorAndThrow(IllegalStateException::new, "inconsistent state expected: {}, found: {}", currentState, priorArcState.orElse(null));
        }

        return ArcStateContext.builder()
                .withArcWorkloadContext(ArcWorkloadContext.builder()
                        .withArcNotifyEvent(stateContext.arcWorkloadContext().arcNotifyEvent())
                        .withRole(stateContext.arcWorkloadContext().role())
                        .build())
                .withPreviousState(stateContext.previousState())
                .withCurrentState(newArcState)
                .withWorkloadError(stateContext.workloadError())
                .build();
    }
}
