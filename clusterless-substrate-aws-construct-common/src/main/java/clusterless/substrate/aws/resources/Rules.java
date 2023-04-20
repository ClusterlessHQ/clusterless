/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.naming.Label;
import clusterless.naming.Region;
import clusterless.naming.Version;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.StagedApp;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

import java.util.Objects;

/**
 *
 */
public class Rules {
    public static Label ruleName(Construct scope, String name) {
        Objects.requireNonNull(name, "name may not be null");

        Label region = Region.of(Stack.of(scope).getRegion());
        Label stage = StagedApp.stagedOf(scope).stage();
        Label project = ManagedProject.projectOf(scope).name();
        Label version = Version.of(ManagedProject.projectOf(scope).version());

        return stage.upperOnly()
                .with(project)
                .with(name)
                .with(version)
                .with(region);
    }
}
