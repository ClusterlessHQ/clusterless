/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.store;

import clusterless.cls.model.deploy.Placement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StoresTest {
    @Test
    void roundTrip() {
        Placement placement = Placement.builder()
                .withStage("dev")
                .withAccount("00000000")
                .withRegion("us-east-1")
                .build();

        assertEquals(placement, Stores.parseBootstrapStoreName(Stores.bootstrapStoreName(StateStore.Meta, placement)));
    }
}
