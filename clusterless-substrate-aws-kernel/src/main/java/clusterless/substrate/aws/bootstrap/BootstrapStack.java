/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.bootstrap;

import clusterless.substrate.aws.managed.Names;
import clusterless.substrate.aws.util.ErrorsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.constructs.Construct;

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

    public BootstrapStack(@NotNull Construct scope, @NotNull String id, @NotNull StackProps props) {
        super(scope, id, props);

        Environment env = props.getEnv();

        String bucketName = Names.bootstrapMetadataBucketName(env);

        Bucket bucket = ErrorsUtil.construct(() -> Bucket.Builder.create(this, bucketName)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .encryption(BucketEncryption.S3_MANAGED)
                .enforceSsl(true)
                .versioned(true)
                .bucketName(bucketName)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build(), LOG);

        new CfnOutput(this, "BootstrapVersion", new CfnOutputProps.Builder()
                .exportName("BootstrapVersion")
                .value(BOOTSTRAP_VERSION)
                .description("bootstrap version")
                .build());

        new CfnOutput(this, "BucketName", new CfnOutputProps.Builder()
                .exportName("BucketName")
                .value(bucket.getBucketArn())
                .description("bootstrap bucket name")
                .build());

        new CfnOutput(this, "BucketDomainName", new CfnOutputProps.Builder()
                .exportName("BucketDomainName")
                .value(bucket.getBucketDomainName())
                .description("bootstrap bucket domain name")
                .build());
    }

}
