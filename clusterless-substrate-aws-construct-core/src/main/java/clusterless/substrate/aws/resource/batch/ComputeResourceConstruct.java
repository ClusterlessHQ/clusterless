/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.batch;

import clusterless.substrate.aws.construct.ResourceConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.resources.Resources;
import clusterless.substrate.aws.resources.Vpcs;
import clusterless.substrate.aws.util.TagsUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.batch.alpha.FargateComputeEnvironment;
import software.amazon.awscdk.services.batch.alpha.IManagedComputeEnvironment;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.constructs.Construct;

/**
 *
 */
public class ComputeResourceConstruct extends ResourceConstruct<ComputeResource> {
    private static final Logger LOG = LogManager.getLogger(ComputeResourceConstruct.class);
    private final IManagedComputeEnvironment computeEnvironment;

    public ComputeResourceConstruct(@NotNull ManagedComponentContext context, @NotNull ComputeResource model) {
        super(context, model, model.computeEnvironmentName());

        String name = Resources.regionallyUniqueProjectName(this, model().computeEnvironmentName());

        computeEnvironment = constructWithinHandler(() ->
                        FargateComputeEnvironment.Builder.create(this, id(model().computeEnvironmentName()))
                                .computeEnvironmentName(name) // globally unique
                                .replaceComputeEnvironment(false)
                                .maxvCpus(4096)
                                .spot(false)
//                        .terminateOnUpdate(false)
//                        .updateTimeout()
//                        .vpcSubnets()
                                .vpc(
                                        Vpc.fromLookup(
                                                this,
                                                "VpcLookup",
                                                VpcLookupOptions.builder()
                                                        .region(context.managedProject().getRegion())
                                                        .vpcName(Vpcs.bootstrapVPCName(this))
                                                        .build()
                                        )
                                )
                                .enabled(true)
                                .build()
        );

        TagsUtil.applyTags(computeEnvironment, model().tags());

        String computeEnvironmentArn = computeEnvironment.getComputeEnvironmentArn();

        addArnFor(model(), (Construct) computeEnvironment, computeEnvironmentArn, "compute environment arn");
    }

    public IManagedComputeEnvironment computeEnvironment() {
        return computeEnvironment;
    }
}
