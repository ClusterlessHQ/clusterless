/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.construct;

import clusterless.cls.managed.component.ArcComponent;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.model.deploy.LocatedDataset;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Project;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.resources.BootstrapStores;
import clusterless.cls.util.Lazy;
import clusterless.commons.naming.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.iam.IGrantable;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.stepfunctions.IChainable;
import software.amazon.awscdk.services.stepfunctions.TaskStateBase;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public abstract class ArcConstruct<M extends Arc<?>> extends ExtensibleConstruct<M> implements ArcComponent {

    private final Lazy<IBucket> manifestBucket = Lazy.of(() -> BootstrapStores.manifestBucket(this));

    public ArcConstruct(@NotNull ManagedComponentContext context, @NotNull M model) {
        super(context, model);
    }

    protected void grantManifestAndDatasetPermissionsTo(IGrantable grantable) {
        grantDatasets(grantable);
        grantManifestReadWrite(grantable);
    }

    protected void grantDatasets(IGrantable grantable) {
        Placement placement = context().deployable().placement();
        Project project = context().deployable().project();
        Map<String, ? extends LocatedDataset> located = context().resolver().locate(placement, project, model().sources());
        grantEachBucket(located, id("Source"), b -> b.grantRead(grantable));
        grantEachBucket(model().sinks(), id("Sink"), b -> b.grantReadWrite(grantable));
    }

    public abstract IChainable createState(String inputPath, String outputPath, IChainable failed, Consumer<TaskStateBase> taskAmendments);

    protected void grantEachBucket(Map<String, ? extends LocatedDataset> datasets, String id, Consumer<IBucket> grant) {
        datasets.entrySet().stream()
                .filter(e -> "s3".equals(e.getValue().pathURI().getScheme()))
                .forEach(e -> {
                    String baseId = Label.of(id).with(e.getKey()).camelCase();
                    String bucketName = e.getValue().pathURI().getHost();
                    grant.accept(getBucketFor(baseId, bucketName));
                });
    }

    public void applyToEachTable(Map<String, ? extends LocatedDataset> datasets, Consumer<URI> apply) {
        datasets.values().stream()
                .filter(d -> "glue".equals(d.pathURI().getScheme()))
                .forEach(d -> apply.accept(d.pathURI()));
    }

    protected void grantManifestRead(@NotNull IGrantable grantee) {
        manifestBucket.get().grantRead(grantee);
    }

    protected void grantManifestReadWrite(@NotNull IGrantable grantee) {
        manifestBucket.get().grantReadWrite(grantee);
    }
}
