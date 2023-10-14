/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.naming.Label;
import clusterless.cls.naming.Region;
import clusterless.cls.naming.Stage;
import clusterless.cls.naming.Version;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Environment;

/**
 *
 */
public class Stacks {
    public static Environment environmentFor(Deployable deployable) {
        return Environment.builder()
                .account(deployable.placement().account())
                .region(deployable.placement().region())
                .build();
    }

    public static Label stackName(@NotNull Deployable deployable, @NotNull Label baseId) {
        Label stage = Stage.of(deployable.placement().stage());
        String name = deployable.project().name();
        Label version = Version.of(deployable.project().version());
        Label region = Region.of(deployable.placement().region());

        return stage
                .with(name)
                .with(baseId)
                .with(version)
                .with(region);
    }

    public static String bootstrapStackName(@NotNull Label stage) {
        return stage.upperOnly().with("ClusterlessBootstrap").lowerHyphen();
    }
}
