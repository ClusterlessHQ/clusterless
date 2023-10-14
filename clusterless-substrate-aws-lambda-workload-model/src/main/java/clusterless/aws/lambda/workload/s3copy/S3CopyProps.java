/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.workload.s3copy;

import clusterless.cls.model.deploy.WorkloadProps;
import clusterless.cls.model.deploy.partial.PathFilter;

public class S3CopyProps extends WorkloadProps {
    PathFilter filter = new PathFilter();

    float failArcOnPartialPercent = 0f;

    public PathFilter filter() {
        return filter;
    }

    public float failArcOnPartialPercent() {
        return failArcOnPartialPercent;
    }
}
