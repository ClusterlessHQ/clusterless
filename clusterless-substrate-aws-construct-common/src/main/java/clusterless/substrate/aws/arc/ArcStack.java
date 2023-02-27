/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc;

import clusterless.config.Configurations;
import clusterless.managed.component.WorkloadComponent;
import clusterless.model.deploy.Arc;
import clusterless.model.deploy.Deployable;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.ManagedStack;
import clusterless.substrate.aws.resources.Stacks;
import clusterless.util.Label;

/**
 *
 */
public class ArcStack extends ManagedStack {
    private Arc arc;

    public ArcStack(Configurations configurations, ManagedProject managedProject, Deployable deployable, Arc arc) {
        super(Stacks.stackName(deployable, arcBaseId(arc)), managedProject, deployable, arcBaseId(arc));
        this.arc = arc;

        ManagedComponentContext context = new ManagedComponentContext(configurations, managedProject, deployable, this);

        ArcOrchestration stateMachine = new ArcOrchestration(context, arc);
        ArcListener arcListener = new ArcListener(context, arc, true);

        arcListener.rule().addTarget(stateMachine.stateMachineTarget());

        ArcMeta.Builder.builder()
                .withArc(arc)
                .withPlacement(deployable.placement())
                .withProject(deployable.project())
                .withArcDeployment(ArcMeta.ArcDeployment.Builder.builder()
                        .withStackName(this.getStackName())
                        .withStepFunctionName(stateMachine.stateMachineName().lowerHyphen())
                        .withListenerRuleName(arcListener.ruleName().lowerHyphen())
//                        .withManifestLocationURIs()
                        .build())
                .build();


    }

    private static Label arcBaseId(Arc arc) {
        return Label.of("Arc").with(arc.workload().name());
    }

    public void applyWorkloadComponent(WorkloadComponent workloadComponent) {
        // set workload target on arc statemachine to the workload component state machine
    }
}
