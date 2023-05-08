/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.batch;

import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Workload;
import clusterless.model.deploy.WorkloadProps;
import clusterless.substrate.aws.props.BatchRuntimeProps;

/**
 *
 */
public class BatchExecArc extends Arc<BatchExecArc.BatchWorkload> {

    public static class BatchWorkload extends Workload<WorkloadProps> {
        BatchRuntimeProps runtimeProps = new BatchRuntimeProps();

        public BatchWorkload() {
            super(new WorkloadProps());
        }

        public BatchRuntimeProps runtimeProps() {
            return runtimeProps;
        }
    }

    public BatchExecArc() {
        super(new BatchWorkload());
    }
}
