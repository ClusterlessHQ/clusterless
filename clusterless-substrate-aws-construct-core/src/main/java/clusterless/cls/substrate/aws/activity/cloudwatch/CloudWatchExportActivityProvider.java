/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.activity.cloudwatch;

import clusterless.cls.managed.component.ActivityComponentService;
import clusterless.cls.managed.component.ProvidesComponent;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:cloudWatchExport",
        synopsis = "Export CloudWatch logs to S3.",
        description = """
                Periodically will extract a CloudWatch log group to an S3 bucket, under a prefix.
                                
                interval: Fourths|Sixth|Twelfths|etc
                    (Future versions will support rates and cron expressions.)
                    
                logGroupName: string
                    The name of the log group to export. e.g '/aws/lambda/my-lambda'
                                
                logStreamPrefix: string (optional)
                    The prefix of the log streams to export.
                                
                bucketRef: string (optional, see below)
                    The reference to the bucket created in this project to export to, created by `aws:core:s3Bucket`.
                                
                pathURI: an s3 URI
                    The destination URI to export to.
                    Use `s3:///prefix` or `/prefix` so that he bucketRef value is used for the bucket name.
                    
                    If a bucket name is given in the URI, the bucket must allow 'logs.amazonaws.com' permission
                    for 's3:GetBucketAcl'.
                """
)
public class CloudWatchExportActivityProvider implements ActivityComponentService<ManagedComponentContext, CloudWatchExportActivity, CloudWatchExportActivityConstruct> {

    @Override
    public CloudWatchExportActivityConstruct create(ManagedComponentContext context, CloudWatchExportActivity model) {
        return new CloudWatchExportActivityConstruct(context, model);
    }

    @Override
    public Class<CloudWatchExportActivity> modelClass() {
        return CloudWatchExportActivity.class;
    }
}
