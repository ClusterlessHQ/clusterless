/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.lambda.StreamResultHandler;
import clusterless.model.state.ArcState;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.substrate.aws.event.ArcStateContext;
import clusterless.util.Env;
import com.amazonaws.services.lambda.runtime.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class ArcStateStartHandler extends StreamResultHandler<ArcNotifyEvent, ArcStateContext> {
    private static final Logger LOG = LogManager.getLogger(ArcStateStartHandler.class);
    protected static final ArcStateProps arcStateProps = Env.fromEnv(
            ArcStateProps.class,
            () -> ArcStateProps.builder()
                    .build()
    );

    ArcStateManager arcStateManager = new ArcStateManager(arcStateProps.arcStatePath());

    public ArcStateStartHandler() {
        super(ArcNotifyEvent.class, ArcStateContext.class);
    }

    protected ArcStateStartObserver observer() {
        return new ArcStateStartObserver() {
        };
    }

    @Override
    public ArcStateContext handleRequest(ArcNotifyEvent event, Context context) {
        logObject("incoming arc event: {}", event);

        ArcStateContext arcStateContext = handleEvent(event, context, observer());

        logObject("outgoing arc context: {}", arcStateContext);

        return arcStateContext;
    }

    protected ArcStateContext handleEvent(ArcNotifyEvent event, Context context, ArcStateStartObserver eventObserver) {
        String lotId = event.lotId();

        // get arc state
        Optional<ArcState> currentState = arcStateManager.findStateFor(lotId);

        // if already running, punt back up to the state machine
        if (currentState.isPresent() && currentState.get() == ArcState.running) {
            LOG.info("lot already running: {}", lotId);
            return ArcStateContext.builder()
                    .withArcNotifyEvent(event)
                    .withPreviousState(ArcState.running)
                    .withCurrentState(ArcState.running)
                    .build();
        }

        // if already completed, punt back up to the state machine
        if (currentState.isPresent() && (currentState.get() == ArcState.complete || currentState.get() == ArcState.missing)) {
            LOG.info("lot already completed: {}", lotId);
            return ArcStateContext.builder()
                    .withArcNotifyEvent(event)
                    .withPreviousState(currentState.get())
                    .withCurrentState(currentState.get())
                    .build();
        }

        if (currentState.isPresent() && currentState.get() == ArcState.partial) {
            LOG.info("lot was partially completed: {}", lotId);
            // todo: get partial manifests and apply to the arc context
        }

        // set to running
        Optional<ArcState> previousState = arcStateManager.setStateFor(lotId, ArcState.running);

        // confirm there isn't some race condition
        // todo: create new exception to capture in state machine
        if (!currentState.equals(previousState)) {
            logErrorAndThrow(IllegalStateException::new, "unexpected state change from: %s, to: %s", currentState.orElse(null), previousState.orElse(null));
        }

        List<String> roles = arcStateProps.sources().entrySet()
                .stream()
                .filter(e -> Objects.equals(e.getValue().name(), event.dataset().name()) && Objects.equals(e.getValue().version(), event.dataset().version()))
                .map(Map.Entry::getKey).collect(Collectors.toList());

        if (roles.size() == 0) {
            logErrorAndThrow(IllegalStateException::new, "no role found for: {}", event.dataset());
        }

        if (roles.size() != 1) {
            logErrorAndThrow(IllegalStateException::new, "too many roles found for: {}, got: {}", event.dataset(), roles);
        }

        // embed notify event
        // create sink manifests identifiers
        // list existing sink partial manifest identifiers
        return ArcStateContext.builder()
                .withArcNotifyEvent(event)
                .withRole(roles.get(0))
                .withPreviousState(currentState.orElse(null)) // partial or null
                .withCurrentState(ArcState.running)
//                .withSinkCompleteManifest()
                .build();
    }

}