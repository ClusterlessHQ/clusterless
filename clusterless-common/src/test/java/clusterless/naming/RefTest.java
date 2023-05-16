/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.naming;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RefTest {
    @Test
    void name() {

        Label label = new Ref()
                .withProvider("aws")
                .withScope("bootstrap")
                .withScopeVersion("20230101")
                .withResourceNs("core")
                .withResourceType("compute")
                .withResourceName("spot")
                .withQualifier(Ref.Qualifier.Id)
                .label();

        Assertions.assertEquals("ref:aws:id:bootstrap:20230101:core:compute:spot", label.lowerColonPath());
    }
}
