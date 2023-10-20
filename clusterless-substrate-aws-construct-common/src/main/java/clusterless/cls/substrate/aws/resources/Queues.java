/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import clusterless.commons.naming.Label;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedApp;
import software.constructs.Construct;

import java.util.Objects;

/**
 *
 */
public class Queues {
    public static Label queueName(Construct scope, String name) {
        Objects.requireNonNull(name, "name may not be null");

        Label stage = ScopedApp.scopedOf(scope).stage();
        Label project = ScopedApp.scopedOf(scope).name();
        Label version = ScopedApp.scopedOf(scope).version();

        return stage.upperOnly()
                .with(project)
                .with(name)
                .with(version);
    }
}
