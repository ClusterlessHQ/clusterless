/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc;

import clusterless.config.Configurations;
import clusterless.managed.component.ArcComponent;
import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.Deployable;
import clusterless.model.deploy.Workload;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.ManagedStack;
import clusterless.substrate.aws.resources.Stacks;
import clusterless.util.Label;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;

import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public class ArcStack extends ManagedStack {
    private Configurations configurations;
    private ManagedProject managedProject;
    private final Deployable deployable;
    private final Arc<? extends Workload> arc;

    private ArcOrchestration orchestration;
    private ArcListener arcListener;

    private static Label arcBaseId(Arc<? extends Workload> arc) {
        return Label.of("Arc").with(arc.name());
    }

    public ArcStack(Configurations configurations, ManagedProject managedProject, Deployable deployable, Arc<? extends Workload> arc) {
        super(Stacks.stackName(deployable, arcBaseId(arc)), managedProject, deployable, arcBaseId(arc));
        this.configurations = configurations;
        this.managedProject = managedProject;
        this.deployable = deployable;
        this.arc = arc;
    }

    public void applyArcWorkloadComponent(ArcComponent arcComponent) {
        ManagedComponentContext context = new ManagedComponentContext(configurations, managedProject, deployable, this);

        orchestration = new ArcOrchestration(context, arc);

        orchestration.buildOrchestrationWith(arcComponent);

//        grantEach(arc.sources(), "Source", b->b.grantRead(orchestration.stateMachine()));
//        grantEach(arc.sinks(), "Sink", b->b.grantReadWrite(orchestration.stateMachine()));
//
//        BootstrapStores.arcStateBucket(this).grantReadWrite(orchestration.stateMachine());
//        BootstrapStores.manifestBucket(this).grantReadWrite(orchestration.stateMachine());

        arcListener = new ArcListener(context, arc, true);

        arcListener.rule().addTarget(orchestration.stateMachineTarget());
    }

    protected void grantEach(Map<String, ? extends Dataset> sources, String id, Consumer<IBucket> grant) {
        sources.forEach((key, value) -> {
            String baseId = Label.of(key).with(id).camelCase();
            String bucketName = value.pathURI().getHost();
            grant.accept(Bucket.fromBucketName(this, baseId, bucketName));
        });
    }

    public ArcMeta arcMeta() {
        return ArcMeta.Builder.builder()
                .withArc(arc)
                .withPlacement(deployable.placement())
                .withProject(deployable.project())
                .withArcDeployment(ArcMeta.ArcDeployment.Builder.builder()
                        .withStackName(this.getStackName())
                        .withStepFunctionName(orchestration.stateMachineName().lowerHyphen())
                        .withListenerRuleName(arcListener.ruleName().lowerHyphen())
                        .build())
                .build();
    }
}
