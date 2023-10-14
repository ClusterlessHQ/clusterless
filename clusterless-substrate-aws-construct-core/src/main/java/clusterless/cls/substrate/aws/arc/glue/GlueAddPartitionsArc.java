/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc.glue;

import clusterless.aws.lambda.workload.glue.GlueAddPartitionsProps;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.model.deploy.Workload;
import clusterless.cls.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.cls.substrate.aws.props.Memory;

/**
 *
 */
public class GlueAddPartitionsArc extends Arc<GlueAddPartitionsArc.AddPartitionWorkload> {

    public static class AddPartitionWorkload extends Workload<GlueAddPartitionsProps> {
        LambdaJavaRuntimeProps runtimeProps = new LambdaJavaRuntimeProps(
                Memory.MEM_512MB,
                3,
                15
        );

        public AddPartitionWorkload() {
            super(new GlueAddPartitionsProps());
        }

        public LambdaJavaRuntimeProps runtimeProps() {
            return runtimeProps;
        }
    }

    public GlueAddPartitionsArc() {
        super(new AddPartitionWorkload());
    }
}
