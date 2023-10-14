/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.arc;

import clusterless.aws.lambda.LocalStackBase;
import clusterless.aws.lambda.TestLots;
import clusterless.cls.model.state.ArcState;
import clusterless.cls.substrate.uri.ArcStateURI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class ArcStateManagerTest extends LocalStackBase {

    @Override
    protected ArcStateProps getProps() {
        return ArcStateProps.builder()
                .withArcStatePath(
                        ArcStateURI.builder()
                                .withPlacement(defaultPlacement())
                                .withProject(defaultProject())
                                .withArcName("test-arc")
                                .build()
                )
                .build();
    }

    /**
     * Embedded in a single test as we don't need to initialize the state between runs
     */
    @Test
    void states() {
        ArcStateManager arcStateManager = new ArcStateManager(getProps().arcStatePath());

        new TestLots().lotStream(5).forEach(lot -> {
            testStateTransition(arcStateManager, null, ArcState.running, ArcState.complete, lot);
            testStateTransition(arcStateManager, ArcState.complete, ArcState.partial, ArcState.missing, lot);
            testStateTransition(arcStateManager, ArcState.missing, ArcState.running, ArcState.partial, lot);
        });
    }

    private static void testStateTransition(ArcStateManager arcStateManager, ArcState originalState, ArcState initialState, ArcState nextState, String lotId) {
        Optional<ArcState> oldState = arcStateManager.setStateFor(lotId, initialState);
        Assertions.assertEquals(originalState, oldState.orElse(null));

        Optional<ArcState> resultState = arcStateManager.findStateFor(lotId);

        Assertions.assertTrue(resultState.isPresent());
        Assertions.assertEquals(initialState, resultState.get());

        resultState = arcStateManager.setStateFor(lotId, nextState);
        Assertions.assertTrue(resultState.isPresent());
        Assertions.assertEquals(initialState, resultState.get());
    }
}
