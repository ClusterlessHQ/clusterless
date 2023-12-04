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
        synopsis = "Export CloudWatch logs.",
        description = """
                Periodically will extract a CloudWatch log group to an S3 bucket.
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
