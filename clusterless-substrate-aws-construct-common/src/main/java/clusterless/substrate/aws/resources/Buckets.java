/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 */
public class Buckets {
    public static final String METADATA_JSON = "metadata.json";

    public static URI bootstrapManifestURI(@NotNull ManagedConstruct managedConstruct, String... names) {
        ManagedProject managedProject = ManagedProject.projectOf(managedConstruct);

        String path = Label.of(managedProject.name())
                .with(managedProject.version())
                .having(names)
                .lowerHyphenPath(true);

        try {
            return new URI("s3", bootstrapManifestBucketName(managedConstruct), path, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("unable to create uri");
        }
    }

    public static String bootstrapMetadataBucketName(@NotNull Construct scope) {
        return bucketName(scope, "Metadata");
    }

    public static String bootstrapArcStateBucketName(@NotNull Construct scope) {
        return bucketName(scope, "ArcState");
    }

    public static String bootstrapManifestBucketName(@NotNull Construct scope) {
        return bucketName(scope, "Manifest");
    }

    private static String bucketName(@NotNull Construct scope, String name) {
        String account = Stack.of(scope).getAccount();
        String region = Stack.of(scope).getRegion();
        Label stage = StagedApp.stagedOf(scope).stage();
        return stage.upperOnly().with("Clusterless")
                .with(name)
                .with(account)
                .with(region)
                .lowerHyphen();
    }

}
