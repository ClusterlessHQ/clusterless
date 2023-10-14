/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.bootstrap;

import clusterless.cls.naming.Label;
import clusterless.cls.naming.Ref;
import clusterless.cls.substrate.aws.cdk.bootstrap.vpc.VPCConstruct;
import clusterless.cls.substrate.aws.managed.StagedApp;
import clusterless.cls.substrate.aws.managed.StagedStack;
import clusterless.cls.substrate.aws.resources.BootstrapStores;
import clusterless.cls.substrate.aws.resources.ClsBootstrap;
import clusterless.cls.substrate.aws.resources.Events;
import clusterless.cls.substrate.aws.util.ErrorsUtil;
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

import static clusterless.cls.substrate.aws.resources.Events.ARC_EVENT_BUS;
import static clusterless.cls.substrate.aws.resources.Events.EVENT_BUS;
import static clusterless.cls.substrate.aws.resources.Vpcs.COMMON_VPC;
import static clusterless.cls.substrate.aws.resources.Vpcs.VPC;
import static clusterless.cls.substrate.store.StateStore.*;

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

        Ref metaRef = Ref.ref().withResourceType(Meta.typeKey()).withResourceName("store");
        addNameRefFor(metaRef, metadataBucketName, "clusterless metadata bucket name");
        addArnRef(metaRef, metadata.getBucketArn(), "clusterless metadata bucket arn");

        Ref arcStateRef = Ref.ref().withResourceType(Arc.typeKey()).withResourceName("store");
        addNameRefFor(arcStateRef, arcStateBucketName, "clusterless arc state bucket name");
        addArnRef(arcStateRef, arcState.getBucketArn(), "clusterless arc state bucket arn");

        Ref manifestRef = Ref.ref().withResourceType(Manifest.typeKey()).withResourceName("store");
        addNameRefFor(manifestRef, manifestBucketName, "clusterless manifest bucket name");
        addArnRef(manifestRef, manifest.getBucketArn(), "clusterless manifest bucket arn");

        Ref eventBusRef = Ref.ref().withResourceType(EVENT_BUS).withResourceName(ARC_EVENT_BUS);
        addNameRefFor(eventBusRef, arcEventBusName, "clusterless arc event bus name");

        Ref vpcRef = Ref.ref().withResourceType(VPC).withResourceName(COMMON_VPC);
        addIdRefFor(vpcRef, vpcConstruct.vpcId(), "clusterless vpc id");
        addArnRef(vpcRef, vpcConstruct.vpcArn(), "clusterless vpc arn");
        addNameRefFor(vpcRef, vpcConstruct.vpcName(), "clusterless vpc name");
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
    protected Ref withContext(Ref ref) {
        return super.withContext(ref)
                .withScope("bootstrap")
                .withScopeVersion(ClsBootstrap.BOOTSTRAP_VERSION)
                .withResourceNs("meta");
    }
}
