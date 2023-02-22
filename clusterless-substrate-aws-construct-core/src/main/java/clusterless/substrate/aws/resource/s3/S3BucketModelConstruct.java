/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.s3;

import clusterless.managed.component.Component;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.model.ModelConstruct;
import clusterless.substrate.aws.util.TagsUtil;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.IBucket;

import java.util.Locale;

/**
 *
 */
public class S3BucketModelConstruct extends ModelConstruct<S3BucketResource> implements Component {
    private final IBucket bucket;

    public S3BucketModelConstruct(@NotNull ManagedComponentContext context, @NotNull S3BucketResource model) {
        super(context, model, model.bucketName());

        bucket = construct(() -> Bucket.Builder.create(this, id(model().bucketName()))
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .encryption(BucketEncryption.S3_MANAGED)
                .enforceSsl(true)
                .versioned(model().versioned())
                .bucketName(model().bucketName())
                .removalPolicy(model().removeOnDestroy() ? RemovalPolicy.DESTROY : RemovalPolicy.RETAIN)
                .autoDeleteObjects(model().removeOnDestroy()) // adds a lambda
                .eventBridgeEnabled(model().eventBridgeEnabled())
                .build());

        TagsUtil.applyTags(bucket, model().tags());

        new CfnOutput(this, id("BucketARN"), new CfnOutputProps.Builder()
                .exportName("s3:%s:arn".formatted(model().bucketName().toLowerCase(Locale.ROOT)))
                .value(bucket().getBucketArn())
                .description("s3 bucket arn")
                .build());
    }

    public IBucket bucket() {
        return bucket;
    }
}
