/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import clusterless.cls.model.deploy.Arc;
import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.naming.Label;
import clusterless.cls.naming.Stage;
import clusterless.cls.naming.Version;
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
