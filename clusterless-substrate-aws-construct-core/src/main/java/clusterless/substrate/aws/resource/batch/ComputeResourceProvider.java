/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.batch;

import clusterless.managed.component.ProvidesComponent;
import clusterless.managed.component.ResourceComponentService;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:computeEnvironment",
        synopsis = "Create an AWS Batch Compute environment to execute Docker images within.",
        description = """
                When using an aws:core:batchExecArc, a compute environment is required to execute the Docker image.
                Currently only Fargate is supported.
                                
                computeEnvironmentName: The unique name of the compute environment to create
                """
)
public class ComputeResourceProvider implements ResourceComponentService<ManagedComponentContext, ComputeResource, ComputeResourceConstruct> {

    @Override
    public ComputeResourceConstruct create(ManagedComponentContext context, ComputeResource model) {
        return new ComputeResourceConstruct(context, model);
    }

    @Override
    public Class<ComputeResource> modelClass() {
        return ComputeResource.class;
    }
}
