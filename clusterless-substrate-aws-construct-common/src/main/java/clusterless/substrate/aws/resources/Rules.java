/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.substrate.aws.managed.StagedApp;
import clusterless.util.Label;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

/**
 *
 */
public class Rules {
    public static String ruleName(Construct scope, String name) {
        String region = Stack.of(scope).getRegion();
        Label stage = StagedApp.stagedOf(scope).stage();
        return stage.upperOnly()
                .with(name)
                .with(region)
                .lowerHyphen();
    }
}