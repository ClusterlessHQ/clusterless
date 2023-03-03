/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc;

import clusterless.managed.component.ArcComponent;
import clusterless.model.deploy.Arc;
import clusterless.substrate.aws.construct.ArcConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.resources.Arcs;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.events.targets.SfnStateMachine;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.stepfunctions.*;

/**
 *
 */
public class ArcOrchestration extends ManagedConstruct implements Orchestration {

    private final Label stateMachineName;
    private StateMachine stateMachine;

    public ArcOrchestration(@NotNull ManagedComponentContext context, @NotNull Arc arc) {
        super(context, Label.of(arc.name()).with("Orchestration"));

        stateMachineName = Arcs.arcBaseName(context().deployable(), arc);
    }

    public void buildOrchestrationWith(ArcComponent arcComponent) {
        Pass head = Pass.Builder.create(this, "Start")
                .outputPath("$.detail")
                .build();

        State workload = ((ArcConstruct<?>) arcComponent).createState();
        head.next(workload);

        ((INextable) workload).next(succeed("Success"));

        stateMachine = StateMachine.Builder.create(this, stateMachineName.camelCase())
                .stateMachineName(stateMachineName.lowerHyphen())
                .stateMachineType(StateMachineType.STANDARD)
                .logs(createLogOptions())
                .definition(head)
                .build();
    }

    @NotNull
    private LogOptions createLogOptions() {
        LogGroup logGroup = LogGroup.Builder.create(this, Label.of("LogGroup").with(stateMachineName).camelCase())
                .removalPolicy(RemovalPolicy.DESTROY)
                .retention(RetentionDays.ONE_DAY)
                .build();

        return LogOptions.builder()
                .destination(logGroup)
                .level(LogLevel.ALL)
                .build();
    }

    public Label stateMachineName() {
        return stateMachineName;
    }

    public StateMachine stateMachine() {
        return stateMachine;
    }

    public SfnStateMachine stateMachineTarget() {
        return SfnStateMachine.Builder.create(stateMachine())
                .build();
    }
}
