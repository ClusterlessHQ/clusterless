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
import clusterless.model.deploy.Deployable;
import clusterless.model.deploy.Workload;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.ManagedStack;
import clusterless.substrate.aws.resources.Stacks;
import clusterless.util.Label;

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
//                        .withManifestLocationURIs()
                        .build())
                .build();
    }

}
