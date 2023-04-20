/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Deployable;
import clusterless.naming.Label;
import clusterless.naming.Stage;
import clusterless.naming.Version;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class Arcs {
    public static Label arcBaseName(@NotNull Deployable deployable, @NotNull Arc<?> arc) {
        Label stage = Stage.of(deployable.placement().stage());
        String project = deployable.project().name();
        Label version = Version.of(deployable.project().version());
        String arcName = arc.name();

        return stage
                .with(project)
                .with(arcName)
                .with(version);
    }
}
