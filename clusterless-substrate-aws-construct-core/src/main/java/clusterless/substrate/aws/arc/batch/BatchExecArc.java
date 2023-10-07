/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.batch;

import clusterless.json.JsonRequiredProperty;
import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Workload;
import clusterless.model.deploy.WorkloadProps;
import clusterless.substrate.aws.props.BatchRuntimeProps;
import clusterless.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.substrate.aws.props.Memory;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class BatchExecArc extends Arc<BatchExecArc.BatchWorkload> {

    public static class BatchWorkload extends Workload<WorkloadProps> {
        BatchRuntimeProps batchRuntimeProps = new BatchRuntimeProps();

        LambdaJavaRuntimeProps lambdaRuntimeProps = new LambdaJavaRuntimeProps(
                Memory.MEM_512MB,
                3,
                15
        );

        @JsonRequiredProperty
        String computeEnvironmentRef;

        @JsonRequiredProperty
        Path imagePath;

        Map<String, String> environment = new LinkedHashMap<>();

        List<String> command = new LinkedList<>();

        public BatchWorkload() {
            super(new WorkloadProps());
        }

        public BatchRuntimeProps batchRuntimeProps() {
            return batchRuntimeProps;
        }

        public LambdaJavaRuntimeProps lambdaRuntimeProps() {
            return lambdaRuntimeProps;
        }

        public String computeEnvironmentRef() {
            return computeEnvironmentRef;
        }

        public Path imagePath() {
            return imagePath;
        }

        public Map<String, String> environment() {
            return environment;
        }

        public List<String> command() {
            return command;
        }
    }

    public BatchExecArc() {
        super(new BatchWorkload());
    }
}
