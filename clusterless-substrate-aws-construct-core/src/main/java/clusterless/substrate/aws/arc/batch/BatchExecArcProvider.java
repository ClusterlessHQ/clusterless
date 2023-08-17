/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.batch;

import clusterless.managed.component.ArcComponentService;
import clusterless.managed.component.ArcLocalExecutor;
import clusterless.managed.component.ProvidesComponent;
import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Workload;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:batchExecArc",
        synopsis = "Create an AWS Batch Exec Arc.",
        description = """
                Allows custom code to be run inside a Docker image on AWS Batch as dataset availability events
                are published.
                When using an aws:core:batchExecArc, a compute environment is required to execute the Docker image.
                Currently only Fargate is supported.

                The "command" is the same as that would be sent to a docker image on execution.
                                
                Values in the command array may be JsonPath expressions back into the current context.
                e.g. ['--lot', '$.arcNotifyEvent.lot', '--manifest', '$.arcNotifyEvent.manifest']
                     
                Available paths:
                    $.arcNotifyEvent.lot - the current lot
                    $.arcNotifyEvent.manifest - URI to the current manifest file
                    $.arcNotifyEvent.dataset.name - the dataset name
                    $.arcNotifyEvent.dataset.version - the dataset version
                    $.arcNotifyEvent.dataset.pathURI - the URI to the root of the dataset

                imagePath: A relative path
                    The path to the Docker image to build.
                                
                environment: {key: value, ...}
                    The environment variables to set in the Docker image.

                command: [command, ...]
                    The command to execute in the Docker image.
                                
                batchRuntimeProps: These only apply to the AWS Batch job that executes the Docker image.
                                                    
                lambdaRuntimeProps: These only apply to the Lambda functions used to manage the AWS Batch
                    jobs within the arc.
                """
)
public class BatchExecArcProvider implements ArcComponentService<ManagedComponentContext, BatchExecArc, BatchExecArcConstruct> {
    @Override
    public BatchExecArcConstruct create(ManagedComponentContext context, BatchExecArc model) {
        return new BatchExecArcConstruct(context, model);
    }

    @Override
    public ArcLocalExecutor executor(Placement placement, Arc<? extends Workload<?>> arc) {
        return new BatchExecArcLocalExecutor(placement, (BatchExecArc) arc);
    }

    @Override
    public Class<BatchExecArc> modelClass() {
        return BatchExecArc.class;
    }
}
