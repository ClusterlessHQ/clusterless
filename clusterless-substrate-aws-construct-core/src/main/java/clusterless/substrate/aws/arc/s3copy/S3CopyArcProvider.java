/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.s3copy;

import clusterless.managed.component.ArcComponentService;
import clusterless.managed.component.ProvidesComponent;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:s3CopyArc",
        synopsis = "The AWS S3 Copy Arc component copies data from one S3 bucket to another S3 bucket.",
        description = """
                All data in the source manifest will be copied to the specified dataset, except those paths that do
                not pass the filter. The filter is a list of include and exclude patterns.
                                
                A common exclude pattern would be '**/_*'. This would exclude all files that start with an underscore,
                like '_SUCCESS' or '_metadata'.
                                
                workloadProps.filter.includes: A list of include patterns.
                workloadProps.filter.excludes: A list of exclude patterns.
                workloadProps.filter.pathSeparator: The path separator to use when matching patterns. Default is '/'.
                workloadProps.filter.ignoreCase: Whether to ignore case when matching patterns. Default is false.
                                
                workloadProps.failArcOnPartialPercent: The percentage of files that can fail before the Arc fails. Default is 0.0.
                """
)
public class S3CopyArcProvider implements ArcComponentService<ManagedComponentContext, S3CopyArc, S3CopyArcConstruct> {
    @Override
    public S3CopyArcConstruct create(ManagedComponentContext context, S3CopyArc model) {
        return new S3CopyArcConstruct(context, model);
    }

    @Override
    public Class<S3CopyArc> modelClass() {
        return S3CopyArc.class;
    }
}
