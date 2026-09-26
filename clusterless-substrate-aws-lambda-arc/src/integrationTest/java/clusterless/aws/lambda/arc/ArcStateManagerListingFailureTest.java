/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.arc;

import clusterless.aws.lambda.LocalStackBase;
import clusterless.aws.lambda.TestLots;
import clusterless.cls.substrate.uri.ArcStateURI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A failed arc state listing must not read as "this lot has never run"; the handler has
 * to fail so the lot is retried rather than acted on with an unknown state.
 */
public class ArcStateManagerListingFailureTest extends LocalStackBase {

    @Override
    protected ArcStateProps getProps() {
        return ArcStateProps.builder()
                .withArcStatePath(
                        ArcStateURI.builder()
                                // no bootstrap bucket exists for this stage, so listing fails
                                .withPlacement(defaultPlacement().withStage("nobucket"))
                                .withProject(defaultProject())
                                .withArcName("test-arc")
                                .build()
                )
                .build();
    }

    @Test
    void listingFailureIsNotNoState() {
        ArcStateManager arcStateManager = new ArcStateManager(getProps().arcStatePath());

        String lotId = new TestLots().lotStream(1).findFirst().orElseThrow();

        Assertions.assertThrows(IllegalStateException.class, () -> arcStateManager.findStateFor(lotId));
    }
}
