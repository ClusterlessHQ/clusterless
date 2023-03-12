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
import clusterless.util.Env;
import com.amazonaws.services.lambda.runtime.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 *
 */
public class ArcStateStartHandler extends StreamResultHandler<ArcNotifyEvent, ArcExecContext> {
    private static final Logger LOG = LogManager.getLogger(ArcStateStartHandler.class);
    protected static final ArcStateProps arcStateProps = Env.fromEnv(
            ArcStateProps.class,
            () -> ArcStateProps.builder()
                    .build()
    );

    ArcStateManager arcStateManager = new ArcStateManager(arcStateProps.arcStatePath());

    public ArcStateStartHandler() {
        super(ArcNotifyEvent.class, ArcExecContext.class);
    }

    protected ArcStateStartObserver observer() {
        return new ArcStateStartObserver() {
        };
    }

    @Override
    public ArcExecContext handleRequest(ArcNotifyEvent event, Context context) {
        logObject("incoming arc event: {}", event);

        ArcExecContext arcExecContext = handleEvent(event, context, observer());

        logObject("outgoing arc context: {}", arcExecContext);

        return arcExecContext;
    }

    protected ArcExecContext handleEvent(ArcNotifyEvent event, Context context, ArcStateStartObserver eventObserver) {
        String lotId = event.lotId();

        // get arc state
        Optional<ArcState> currentState = arcStateManager.findStateFor(lotId);

        // if already running, punt back up to the state machine
        if (currentState.isPresent() && currentState.get() == ArcState.running) {
            LOG.info("lot already running: {}", lotId);
            return ArcExecContext.builder()
                    .withArcNotifyEvent(event)
                    .withPreviousState(ArcState.running)
                    .withCurrentState(ArcState.running)
                    .build();
        }

        // if already completed, punt back up to the state machine
        if (currentState.isPresent() && currentState.get() == ArcState.complete) {
            LOG.info("lot already completed: {}", lotId);
            return ArcExecContext.builder()
                    .withArcNotifyEvent(event)
                    .withPreviousState(ArcState.complete)
                    .withCurrentState(ArcState.complete)
                    .build();
        }

        // set to running
        Optional<ArcState> previousState = arcStateManager.setStateFor(lotId, ArcState.running);

        // confirm there isn't some race condition
        // todo: create new exception to capture in state machine
        if (!currentState.equals(previousState)) {
            logErrorAndThrow(IllegalStateException::new, "unexpected state change from: %s, to: %s", currentState.orElse(null), previousState.orElse(null));
        }

        // embed notify event
        // create sink manifests identifiers
        // list existing sink partial manifest identifiers
        return ArcExecContext.builder()
                .withArcNotifyEvent(event)
                .withPreviousState(currentState.orElse(null))
                .withCurrentState(ArcState.running)
//                .withSinkCompleteManifest()
                .build();
    }

}