/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.model.deploy.Dataset;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.util.Label;
import clusterless.util.Partition;
import clusterless.util.URIs;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static clusterless.substrate.aws.resources.Buckets.BootstrapBucket.*;

/**
 *
 */
public class Buckets {
    public enum BootstrapBucket implements Label {
        METADATA("Metadata"),
        ARC_STATE("ArcState"),
        MANIFEST("Manifest");

        final String value;

        BootstrapBucket(String value) {
            this.value = value;
        }

        @Override
        public String camelCase() {
            return value;
        }

        Label bucketNameKey() {
            return this.with("BucketName");
        }
    }

    public static final Label ARC_STATE_BUCKET_NAME = ARC_STATE.bucketNameKey();
    public static final Label MANIFEST_BUCKET_NAME = MANIFEST.bucketNameKey();


    public static URI manifestPath(@NotNull ManagedConstruct managedConstruct, ManifestState state, Dataset dataset) {
        return manifestPath(managedConstruct, state, dataset.name(), dataset.version());
    }

    protected static URI manifestPath(@NotNull ManagedConstruct managedConstruct, ManifestState manifestState, String... names) {
        ManagedProject managedProject = ManagedProject.projectOf(managedConstruct);

        Partition attempt = manifestState.hasAttempts() ? Partition.namedOf("attempt", System.currentTimeMillis()) : Partition.NULL;

        String path = Partition.of(managedProject.name())
                .with(managedProject.version())
                .having(names)
                .with(manifestState)
                .with(attempt)
                .path();

        try {
            return new URI("s3", manifestBucketName(managedConstruct), URIs.normalize(path), null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("unable to create uri", e);
        }
    }

    public static String metadataBucketName(@NotNull Construct scope) {
        return bootstrapBucketName(scope, METADATA);
    }

    public static String arcStateBucketName(@NotNull Construct scope) {
        return bootstrapBucketName(scope, ARC_STATE);
    }

    public static String arcStateBucketNameRef(@NotNull Construct scope) {
        return bootstrapBucketNameRef(scope, ARC_STATE);
    }

    public static String manifestBucketName(@NotNull Construct scope) {
        return bootstrapBucketName(scope, MANIFEST);
    }

    public static String manifestBucketNameRef(@NotNull Construct scope) {
        return bootstrapBucketNameRef(scope, MANIFEST);
    }

    private static String bootstrapBucketName(@NotNull Construct scope, BootstrapBucket name) {
        String account = Stack.of(scope).getAccount();
        String region = Stack.of(scope).getRegion();
        Label stage = StagedApp.stagedOf(scope).stage();
        return bootstrapBucketName(name, account, region, stage);
    }

    private static String bootstrapBucketNameRef(@NotNull Construct scope, BootstrapBucket name) {
        Label stage = StagedApp.stagedOf(scope).stage();
        return Fn.importValue(stage.with(name.bucketNameKey()).lowerHyphen());
    }

    public static String bootstrapBucketName(BootstrapBucket name, String account, String region, String stage) {
        return bootstrapBucketName(name, account, region, Label.of(stage));
    }

    protected static String bootstrapBucketName(BootstrapBucket name, String account, String region, Label stage) {
        Objects.requireNonNull(name, "bucket meta-name");
        Objects.requireNonNull(account, "account");
        Objects.requireNonNull(region, "region");

        return stage.with("Clusterless")
                .with(name)
                .with(account)
                .with(region)
                .lowerHyphen();
    }
}
