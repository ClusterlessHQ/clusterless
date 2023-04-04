/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.util.Label;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

import java.util.Objects;

/**
 *
 */
public class Rules {
    public static Label ruleName(Construct scope, String name) {
        Objects.requireNonNull(name, "name may not be null");

        String region = Stack.of(scope).getRegion();
        Label stage = StagedApp.stagedOf(scope).stage();
        Label project = ManagedProject.projectOf(scope).name();
        String version = ManagedProject.projectOf(scope).version();

        return stage.upperOnly()
                .with(project)
                .with(name)
                .with(version)
                .with(region);
    }
}
