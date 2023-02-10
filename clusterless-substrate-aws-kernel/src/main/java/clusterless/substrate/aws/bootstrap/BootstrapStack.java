/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.bootstrap;

import clusterless.json.JSONUtil;
import clusterless.model.bootstrap.Metadata;
import clusterless.substrate.aws.managed.StagedApp;
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
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.ISource;
import software.amazon.awscdk.services.s3.deployment.Source;

import java.util.List;
import java.util.Objects;

/**
 * keys to consider exporting
 * - BootstrapVersion
 * - BucketDomainName
 * - BucketName
 * - ImageRepositoryName
 */
public class BootstrapStack extends Stack {
    private static final Logger LOG = LogManager.getLogger(BootstrapStack.class);
    public static final String BOOTSTRAP_VERSION = "1";
    private BucketDeployment deployment;
    private EventBus eventBus;

    public BootstrapStack(@NotNull StagedApp app, @NotNull StackProps props) {
        super(app, "ClusterlessBootstrapStack", props);

        constructStack(app, props);
    }

    public BucketDeployment deployment() {
        return deployment;
    }

    public EventBus eventBus() {
        return eventBus;
    }

    protected void constructStack(@NotNull StagedApp app, @NotNull StackProps props) {
        Environment env = props.getEnv();

        Objects.requireNonNull(env);

        Label stage = app.stage();
        String bootstrapVersion = stage.with("BootstrapVersion").camelCase();
        new CfnOutput(this, bootstrapVersion, new CfnOutputProps.Builder()
                .exportName(bootstrapVersion)
                .value(BOOTSTRAP_VERSION)
                .description("bootstrap version")
                .build());

        String metadataBucketName = Buckets.bootstrapMetadataBucketName(this);
        String arcStateBucketName = Buckets.bootstrapArcStateBucketName(this);
        String manifestBucketName = Buckets.bootstrapManifestBucketName(this);

        String arcEventBusName = Events.arcEventBusName(this);

        eventBus = EventBus.Builder.create(this, "ArcEventBus")
                .eventBusName(arcEventBusName)
                .build();

        Bucket metadataBucket = constructSharedBucket(metadataBucketName, stage.with("Metadata"));
        constructSharedBucket(arcStateBucketName, stage.with("ArcState"));
        constructSharedBucket(manifestBucketName, stage.with("Manifest"));

        Metadata metadata = new Metadata(stage.upperOnly().camelCase(), BOOTSTRAP_VERSION, arcStateBucketName, manifestBucketName);

        ISource metadataJson = Source.jsonData(Buckets.METADATA_JSON, JSONUtil.writeAsStringSafe(metadata));

        deployment = BucketDeployment.Builder.create(this, "MetadataDeployment")
                .destinationBucket(metadataBucket)
                .prune(false)
                .retainOnDelete(false)
                .sources(List.of(metadataJson))
                .build();
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

        String nameLabel = prefix.with("BucketName").camelCase();
        new CfnOutput(this, nameLabel, new CfnOutputProps.Builder()
                .exportName(nameLabel)
                .value(bucket.getBucketArn())
                .description("bootstrap %s bucket name".formatted(prefix))
                .build());

        String domainLabel = prefix.with("BucketDomainName").camelCase();
        new CfnOutput(this, domainLabel, new CfnOutputProps.Builder()
                .exportName(domainLabel)
                .value(bucket.getBucketDomainName())
                .description("bootstrap %s bucket domain name".formatted(prefix))
                .build());

        return bucket;
    }
}
