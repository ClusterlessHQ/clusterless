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
import clusterless.substrate.aws.event.ArcExecContext;
import clusterless.substrate.aws.event.ArcStateContext;
import clusterless.substrate.aws.uri.ManifestURI;
import clusterless.substrate.aws.uri.StateURI;
import clusterless.util.Env;
import com.amazonaws.services.lambda.runtime.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class ArcStateCompleteHandler extends StreamResultHandler<ArcStateContext, ArcStateContext> {
    private static final Logger LOG = LogManager.getLogger(ArcStateCompleteHandler.class);

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
        };
    }

    @Override
    public ArcStateContext handleRequest(ArcStateContext event, Context context) {
        logObject("incoming arc event: {}", event);

        ArcStateContext arcStateContext = handleEvent(event, context, observer());

        logObject("outgoing arc context: {}", arcStateContext);

        return arcStateContext;
    }

    protected ArcStateContext handleEvent(ArcStateContext event, Context context, ArcStateCompleteObserver eventObserver) {
        // check status
        Map<String, URI> sinkManifests = event.sinkManifests();

        if (sinkManifests == null || sinkManifests.isEmpty()) {
            return logErrorAndThrow(IllegalStateException::new, "workload sink states is empty");
        }

        eventObserver.applySinkManifests(sinkManifests);

        List<ManifestURI> manifestURIS = sinkManifests.values().stream().map(ManifestURI::parse).collect(Collectors.toList());

        Set<ManifestState> states = manifestURIS.stream().map(StateURI::state).collect(Collectors.toSet());

        ArcState newArcState = null;

        if (states.contains(ManifestState.partial)) {
            LOG.info("workload had partial results, marking partial");
            newArcState = ArcState.partial;
        } else if (states.size() == 1 && states.contains(ManifestState.empty)) {
            LOG.info("workload has empty results, marking complete");
            newArcState = ArcState.complete;
        } else if (states.contains(ManifestState.complete)) {
            LOG.info("workload has complete results");
            newArcState = ArcState.complete;
        }

        if (newArcState == null) {
            return logErrorAndThrow(IllegalStateException::new, "unexpected result states: {}", sinkManifests);
        }

        String lotId = event.arcNotifyEvent().lotId();

        // push notify on each sink
        for (Map.Entry<String, URI> entry : sinkManifests.entrySet()) {
            String role = entry.getKey();
            URI manifestURI = entry.getValue();

            eventPublishers.get(role).publishEvent(lotId, manifestURI);
        }

        // change state to complete/partial
        Optional<ArcState> priorArcState = arcStateManager.setStateFor(lotId, newArcState);

        // confirm there isn't some race condition
        // todo: create new exception to capture in state machine
        // this should always be running state
        ArcState currentState = event.currentState();
        if (currentState == ArcState.running && currentState != priorArcState.orElse(null)) {
            return logErrorAndThrow(IllegalStateException::new, "unexpected state change from: {}, to: {}", currentState, priorArcState.orElse(null));
        }

        return ArcStateContext.builder()
                .withArcExecContext(ArcExecContext.builder()
                        .withArcNotifyEvent(event.arcNotifyEvent())
                        .withRole(event.role())
                        .build())
                .withPreviousState(event.previousState())
                .withCurrentState(newArcState)
                .build();
    }
}
