/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.construct;

import clusterless.managed.component.ArcComponent;
import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Dataset;
import clusterless.naming.Label;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.resources.BootstrapStores;
import clusterless.util.Lazy;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.iam.IGrantable;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.stepfunctions.IChainable;
import software.amazon.awscdk.services.stepfunctions.State;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public abstract class ArcConstruct<M extends Arc<?>> extends ModelConstruct<M> implements ArcComponent {

    private final Lazy<IBucket> manifestBucket = Lazy.of(() -> BootstrapStores.manifestBucket(this));
    private final Map<String, IBucket> buckets = new HashMap<>(); // cache the construct to prevent collisions

    public ArcConstruct(@NotNull ManagedComponentContext context, @NotNull M model) {
        super(context, model, model.name());
    }

    protected void grantManifestAndDatasetPermissionsTo(IGrantable grantable) {
        grantDatasets(grantable);
        grantManifestReadWrite(grantable);
    }

    protected void grantDatasets(IGrantable grantable) {
        grantEach(model().sources(), id("Source"), b -> b.grantRead(grantable));
        grantEach(model().sinks(), id("Sink"), b -> b.grantReadWrite(grantable));
    }

    public abstract IChainable createState(String inputPath, String outputPath, State failed);

    protected void grantEach(Map<String, ? extends Dataset> sources, String id, Consumer<IBucket> grant) {
        sources.forEach((key, value) -> {
            String baseId = Label.of(id).with(key).camelCase();
            String bucketName = value.pathURI().getHost();
            grant.accept(getBucketFor(baseId, bucketName));
        });
    }

    @NotNull
    protected IBucket getBucketFor(String baseId, String bucketName) {
        return buckets.computeIfAbsent(bucketName, k -> Bucket.fromBucketName(this, baseId, k));
    }

    protected void grantManifestRead(@NotNull IGrantable grantee) {
        manifestBucket.get().grantRead(grantee);
    }

    protected void grantManifestReadWrite(@NotNull IGrantable grantee) {
        manifestBucket.get().grantReadWrite(grantee);
    }
}
