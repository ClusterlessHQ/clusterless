/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.manifest;

import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.model.state.ArcState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StateTest {
    @Test
    void arc() {
        Assertions.assertEquals(ArcState.partial, ArcState.parse("state=partial"));
        Assertions.assertEquals(ArcState.partial, ArcState.parse("STATE=PARTIAL"));
        Assertions.assertEquals(ArcState.partial, ArcState.parse("partial"));
        Assertions.assertEquals(ArcState.partial, ArcState.parse("partial.arc"));
    }

    @Test
    void manifest() {
        Assertions.assertEquals(ManifestState.partial, ManifestState.parse("state=partial"));
        Assertions.assertEquals(ManifestState.partial, ManifestState.parse("STATE=PARTIAL"));
        Assertions.assertEquals(ManifestState.partial, ManifestState.parse("partial"));
    }
}
