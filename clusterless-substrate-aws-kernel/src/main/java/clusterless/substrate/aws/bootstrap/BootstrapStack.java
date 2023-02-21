/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.bootstrap;

import clusterless.substrate.aws.managed.StagedApp;
import clusterless.substrate.aws.managed.StagedStack;
import clusterless.substrate.aws.resources.Buckets;
import clusterless.substrate.aws.resources.Events;
import clusterless.substrate.aws.util.ErrorsUtil;
import clusterless.util.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;

import java.util.Objects;

/**
 * keys to consider exporting
 * - BootstrapVersion
 * - BucketDomainName
 * - BucketName
 * - ImageRepositoryName
 */
public class BootstrapStack extends StagedStack {
    private static final Logger LOG = LogManager.getLogger(BootstrapStack.class);
    public static final String BOOTSTRAP_VERSION = "1";
    private EventBus eventBus;

    public BootstrapStack(@NotNull StagedApp app, @NotNull StackProps props) {
        super(app, "ClusterlessBootstrapStack", props);

        constructStack(app, props);
    }

    public EventBus eventBus() {
        return eventBus;
    }

    protected void constructStack(@NotNull StagedApp app, @NotNull StackProps props) {
        Environment env = props.getEnv();

        Objects.requireNonNull(env);

        String metadataBucketName = Buckets.bootstrapMetadataBucketName(this);
        String arcStateBucketName = Buckets.bootstrapArcStateBucketName(this);
        String manifestBucketName = Buckets.bootstrapManifestBucketName(this);

        String arcEventBusName = Events.arcEventBusName(this);

        eventBus = EventBus.Builder.create(this, "ArcEventBus")
                .eventBusName(arcEventBusName)
                .build();

        constructSharedBucket(metadataBucketName, stage().with("Metadata"));
        constructSharedBucket(arcStateBucketName, stage().with("ArcState"));
        constructSharedBucket(manifestBucketName, stage().with("Manifest"));

        createOutputFor(stage().with("BootstrapVersion"), BOOTSTRAP_VERSION, "clusterless bootstrap version");
        createOutputFor(stage().with(Events.ARC_EVENT_BUS_NAME), arcEventBusName, "clusterless arc event bus name");
        createOutputFor(stage().with(Buckets.ARC_STATE_BUCKET_NAME), arcStateBucketName, "clusterless arc state bucket name");
        createOutputFor(stage().with(Buckets.MANIFEST_BUCKET_NAME), manifestBucketName, "clusterless manifest bucket name");
    }

    protected void createOutputFor(Label name, String value, String description) {
        LOG.info("creating output for: {}", name);

        new CfnOutput(this, name.camelCase(), new CfnOutputProps.Builder()
                .exportName(name.lowerHyphen())
                .value(value)
                .description(description)
                .build());
    }

    private Bucket constructSharedBucket(String bucketName, Label prefix) {
        LOG.info("initializing {} bucket: {}", prefix.lowerHyphen(), bucketName);

        Bucket bucket = ErrorsUtil.construct(() -> Bucket.Builder.create(this, bucketName)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .encryption(BucketEncryption.S3_MANAGED)
                .enforceSsl(true)
                .versioned(true)
                .bucketName(bucketName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build(), LOG);

        return bucket;
    }
}
