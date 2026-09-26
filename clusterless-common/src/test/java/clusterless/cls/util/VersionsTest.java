/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * version.properties is only written into clusterless-main's resources, so it is absent here,
 * as it is when running from an IDE or the docs generator.
 */
public class VersionsTest {
    @Test
    void missingVersionResourceFallsBackToWip() {
        Assertions.assertNull(Versions.class.getClassLoader().getResource("version.properties"), "test expects the resource to be absent");

        Assertions.assertEquals(Versions.WIP, Versions.clsVersion());
    }
}
