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
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class Arcs {
    public static Label arcBaseName(@NotNull Deployable deployable, @NotNull Arc arc) {
        String stage = deployable.placement().stage();
        String name = deployable.project().name();
        String version = deployable.project().version();
        String region = deployable.placement().region();

        String arcName = arc.name();

        return Label.of(stage).upperOnly()
                .with(Label.of(name))
                .with(arcName)
                .with(Label.of(version))
                .with(Label.of(region));
    }
}
