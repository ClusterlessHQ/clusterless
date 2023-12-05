/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.s3;

import clusterless.cls.config.CommonConfig;
import clusterless.cls.substrate.aws.construct.ResourceConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.resources.Buckets;
import clusterless.cls.substrate.aws.util.TagsUtil;
import clusterless.commons.naming.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.Construct;

/**
 *
 */
public class S3BucketResourceConstruct extends ResourceConstruct<S3BucketResource> {
    private static final Logger LOG = LogManager.getLogger(S3BucketResourceConstruct.class);
    private final IBucket bucket;

    public S3BucketResourceConstruct(@NotNull ManagedComponentContext context, @NotNull S3BucketResource model) {
        super(context, model, Label.of(model.bucketName()));

        CommonConfig config = context.configurations().get("common");

        boolean removeOnDestroy = config.resource().removeAllOnDestroy() || model().removeOnDestroy();

        if (removeOnDestroy) {
            LOG.info("resource: {}, and all objects will be removed on destroy: {}", model().bucketName(), removeOnDestroy);
        }

        bucket = constructWithinHandler(() -> Bucket.Builder.create(this, id(model().bucketName()))
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .encryption(BucketEncryption.S3_MANAGED)
                .enforceSsl(true) // adds a bucket policy on aws:SecureTransport
                .versioned(model().versioned())
                .bucketName(Buckets.verifyBucketName(model().bucketName()))
                .removalPolicy(removeOnDestroy ? RemovalPolicy.DESTROY : RemovalPolicy.RETAIN)
                .autoDeleteObjects(removeOnDestroy) // cdk adds a lambda if true
                // as of 2.64.0 a lambda is installed -> https://github.com/aws/aws-cdk/issues/24086
                .eventBridgeEnabled(model().enableEventBridge())
                .build());

        TagsUtil.applyTags(bucket, model().tags());

        exportArnRefFor(model(), (Construct) bucket, bucket().getBucketArn(), "s3 bucket arn");
    }

    public IBucket bucket() {
        return bucket;
    }
}
