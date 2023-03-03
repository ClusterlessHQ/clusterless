/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.s3copy;

import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Workload;
import clusterless.substrate.aws.arc.props.LambdaJavaRuntimeProps;

/**
 *
 */
public class S3CopyArc extends Arc<S3CopyArc.CopyWorkload> {

    public static class CopyWorkload extends Workload {
        LambdaJavaRuntimeProps runtimeProps = new LambdaJavaRuntimeProps(
                LambdaJavaRuntimeProps.MEM_512MB,
                3,
                15
        );

        public LambdaJavaRuntimeProps runtimeProps() {
            return runtimeProps;
        }
    }


    public S3CopyArc() {
        super(new CopyWorkload());
    }
}
