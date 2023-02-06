/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.model.Deploy;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Environment;

import java.util.Objects;

/**
 *
 */
public class Names {
    public static Label stackName(@NotNull Deploy deploy, @NotNull Label baseId) {
        String stage = deploy.placement().stage();
        String name = deploy.project().name();
        String version = deploy.project().version();
        String region = deploy.placement().region();

        return Label.of(stage).upperOnly()
                .with(Label.of(name))
                .with(baseId)
                .with(Label.of(version))
                .with(Label.of(region));
    }

    public static String bootstrapMetadataBucketName(@NotNull Environment env) {
        Objects.requireNonNull(env, "Environment may not be null");

        return Label.of("Clusterless")
                .with(Label.of("Metadata"))
                .with(Label.of(env.getAccount()))
                .with(Label.of(env.getRegion()))
                .lowerHyphen();
    }
}
