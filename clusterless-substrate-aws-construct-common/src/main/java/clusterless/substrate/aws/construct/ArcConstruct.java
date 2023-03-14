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
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.resources.BootstrapStores;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.iam.IGrantable;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.stepfunctions.State;

import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public abstract class ArcConstruct<M extends Arc<?>> extends ModelConstruct<M> implements ArcComponent {
    public ArcConstruct(@NotNull ManagedComponentContext context, @NotNull M model) {
        super(context, model, model.name());
    }

    protected void grantPermissionsTo(IGrantable grantable) {
        grantDatasets(grantable);
        grantManifestReadWrite(grantable);
    }

    protected void grantDatasets(IGrantable grantable) {
        grantEach(model().sources(), id("Source"), b -> b.grantRead(grantable));
        grantEach(model().sinks(), id("Sink"), b -> b.grantReadWrite(grantable));
    }

    public abstract State createState(String outputPath, State failed);

    protected void grantEach(Map<String, ? extends Dataset> sources, String id, Consumer<IBucket> grant) {
        sources.forEach((key, value) -> {
            String baseId = Label.of(id).with(key).camelCase();
            String bucketName = value.pathURI().getHost();
            grant.accept(Bucket.fromBucketName(this, baseId, bucketName));
        });
    }

    protected void grantManifestReadWrite(@NotNull IGrantable grantee) {
        BootstrapStores.manifestBucket(this).grantReadWrite(grantee);
    }
}
