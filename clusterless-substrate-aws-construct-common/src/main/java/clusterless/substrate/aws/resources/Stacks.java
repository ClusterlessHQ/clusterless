/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.model.deploy.Deployable;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class Stacks {
    public static Label stackName(@NotNull Deployable deployable, @NotNull Label baseId) {
        String stage = deployable.placement().stage();
        String name = deployable.project().name();
        String version = deployable.project().version();
        String region = deployable.placement().region();

        return Label.of(stage).upperOnly()
                .with(Label.of(name))
                .with(baseId)
                .with(Label.of(version))
                .with(Label.of(region));
    }

    public static String bootstrapStackName(@NotNull Label stage) {
        return stage.upperOnly().with("ClusterlessBootstrap").lowerHyphen();
    }
}
