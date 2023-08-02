/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.s3;

import clusterless.managed.component.ProvidesComponent;
import clusterless.managed.component.ResourceComponentService;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:s3Bucket",
        synopsis = "Create an AWS S3 Bucket.",
        description = """
                A simple way to create a managed S3 buckets within a project.
                                
                The bucket will be configured with the following:
                    .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                    .encryption(BucketEncryption.S3_MANAGED)
                    .enforceSsl(true)
                                
                bucketName: a globally unique name
                    The unique name of the S3 bucket to create.
                                
                versioned: true|false
                    Whether this bucket should have versioning turned on or not.
                                
                enableEventBridge: true|false
                    Whether this bucket should send notifications to Amazon EventBridge or not.
                    As of CDK 2.64.0 a lambda is installed: https://github.com/aws/aws-cdk/issues/24086
                    
                removeOnDestroy: true|false
                    Configured with the following:
                        .removalPolicy(removeOnDestroy ? RemovalPolicy.DESTROY : RemovalPolicy.RETAIN)
                        .autoDeleteObjects(removeOnDestroy) // cdk adds a lambda if true
                                    
                tags: { key: value, ... }
                    Tags to apply to the bucket.
                """
)
public class S3BucketResourceProvider implements ResourceComponentService<ManagedComponentContext, S3BucketResource, S3BucketResourceConstruct> {

    @Override
    public S3BucketResourceConstruct create(ManagedComponentContext context, S3BucketResource model) {
        return new S3BucketResourceConstruct(context, model);
    }

    @Override
    public Class<S3BucketResource> modelClass() {
        return S3BucketResource.class;
    }
}
