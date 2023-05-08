/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.bootstrap;

import clusterless.naming.ExportRef;
import clusterless.naming.Label;
import clusterless.substrate.aws.bootstrap.vpc.VPCConstruct;
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.substrate.aws.managed.StagedStack;
import clusterless.substrate.aws.resources.BootstrapStores;
import clusterless.substrate.aws.resources.ClsBootstrap;
import clusterless.substrate.aws.resources.Events;
import clusterless.substrate.aws.util.ErrorsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;

import java.util.Objects;

import static clusterless.substrate.aws.resources.Events.ARC_EVENT_BUS;
import static clusterless.substrate.aws.resources.Events.EVENT_BUS;
import static clusterless.substrate.aws.resources.Vpcs.COMMON_VPC;
import static clusterless.substrate.aws.resources.Vpcs.VPC;
import static clusterless.substrate.aws.store.StateStore.*;

/**
 * keys to consider exporting
 * - BootstrapVersion
 * - BucketDomainName
 * - BucketName
 * - ImageRepositoryName
 */
public class BootstrapStack extends StagedStack {
    private static final Logger LOG = LogManager.getLogger(BootstrapStack.class);

    public BootstrapStack(@NotNull StagedApp app, @NotNull StackProps props) {
        super(app, "ClusterlessBootstrapStack", props);

        constructStack(app, props);
    }

    protected void constructStack(@NotNull StagedApp app, @NotNull StackProps props) {
        Environment env = props.getEnv();

        Objects.requireNonNull(env);

        String metadataBucketName = BootstrapStores.metadataStoreName(this);
        String arcStateBucketName = BootstrapStores.arcStateStoreName(this);
        String manifestBucketName = BootstrapStores.manifestStoreName(this);

        String arcEventBusName = Events.arcEventBusName(this);

        EventBus.Builder.create(this, "ArcEventBus")
                .eventBusName(arcEventBusName)
                .build();

        VPCConstruct vpcConstruct = new VPCConstruct(this);

        Bucket metadata = constructSharedBucket(metadataBucketName, stage().with("Metadata"));
        Bucket arcState = constructSharedBucket(arcStateBucketName, stage().with("ArcState"));
        Bucket manifest = constructSharedBucket(manifestBucketName, stage().with("Manifest"));

        BootstrapMeta bootstrapMeta = (BootstrapMeta) app.deployMeta();

        bootstrapMeta.setVersion(ClsBootstrap.BOOTSTRAP_VERSION);

        ExportRef metaRef = ExportRef.ref().withResourceType(Meta.typeKey()).withResourceName(Meta.storeKey());
        addNameFor(metaRef, metadataBucketName, "clusterless metadata bucket name");
        addArnFor(metaRef, metadata.getBucketArn(), "clusterless metadata bucket arn");

        ExportRef arcStateRef = ExportRef.ref().withResourceType(Arc.typeKey()).withResourceName(Arc.storeKey());
        addNameFor(arcStateRef, arcStateBucketName, "clusterless arc state bucket name");
        addArnFor(arcStateRef, arcState.getBucketArn(), "clusterless arc state bucket arn");

        ExportRef manifestRef = ExportRef.ref().withResourceType(Manifest.typeKey()).withResourceName(Manifest.storeKey());
        addNameFor(manifestRef, manifestBucketName, "clusterless manifest bucket name");
        addArnFor(manifestRef, manifest.getBucketArn(), "clusterless manifest bucket arn");

        ExportRef eventBusRef = ExportRef.ref().withResourceType(EVENT_BUS).withResourceName(ARC_EVENT_BUS);
        addNameFor(eventBusRef, arcEventBusName, "clusterless arc event bus name");

        ExportRef vpcRef = ExportRef.ref().withResourceType(VPC).withResourceName(COMMON_VPC);
        addIdFor(vpcRef, vpcConstruct.vpcId(), "clusterless vpc id");
        addArnFor(vpcRef, vpcConstruct.vpcArn(), "clusterless vpc arn");
    }

    private Bucket constructSharedBucket(String bucketName, Label prefix) {
        LOG.info("initializing {} bucket: {}", prefix.lowerHyphen(), bucketName);

        return ErrorsUtil.construct(() -> Bucket.Builder.create(this, bucketName)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .encryption(BucketEncryption.S3_MANAGED)
                .enforceSsl(true)
                .versioned(true)
                .bucketName(bucketName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build(), LOG);
    }

    @Override
    protected ExportRef withContext(ExportRef ref) {
        return super.withContext(ref)
                .withScope("bootstrap")
                .withScopeVersion(ClsBootstrap.BOOTSTRAP_VERSION);
    }
}
