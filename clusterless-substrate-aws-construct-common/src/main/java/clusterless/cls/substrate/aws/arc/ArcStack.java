/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc;

import clusterless.cls.config.Configurations;
import clusterless.cls.managed.component.ArcComponent;
import clusterless.cls.managed.dataset.DatasetResolver;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.model.deploy.Workload;
import clusterless.cls.naming.Label;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.managed.ManagedProject;
import clusterless.cls.substrate.aws.managed.ManagedStack;
import clusterless.cls.substrate.aws.resources.Stacks;

/**
 *
 */
public class ArcStack extends ManagedStack {
    private final Configurations configurations;
    private final DatasetResolver resolver;
    private final ManagedProject managedProject;
    private final Deployable deployable;
    private final Arc<? extends Workload<?>> arc;

    private ArcOrchestration orchestration;
    private ArcListener arcListener;

    private static Label arcBaseId(Arc<? extends Workload<?>> arc) {
        return Label.of("Arc").with(arc.name());
    }

    public ArcStack(Configurations configurations, DatasetResolver resolver, ManagedProject managedProject, Deployable deployable, Arc<? extends Workload> arc) {
        super(Stacks.stackName(deployable, arcBaseId(arc)), managedProject, deployable, arcBaseId(arc));
        this.configurations = configurations;
        this.resolver = resolver;
        this.managedProject = managedProject;
        this.deployable = deployable;
        this.arc = arc;
    }

    public void applyArcWorkloadComponent(ArcComponent arcComponent) {
        ManagedComponentContext context = new ManagedComponentContext(configurations, resolver, managedProject, deployable, this);

        orchestration = new ArcOrchestration(context, arc);

        orchestration.buildOrchestrationWith(arcComponent);

        arcListener = new ArcListener(context, arc, true);

        arcListener.rule().addTarget(orchestration.stateMachineTarget());
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
