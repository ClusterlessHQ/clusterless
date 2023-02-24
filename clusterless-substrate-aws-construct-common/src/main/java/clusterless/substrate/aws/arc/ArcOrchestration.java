/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc;

import clusterless.model.deploy.Arc;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.resources.Workloads;
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

    private final StateMachine stateMachine;
    private final Label stateMachineName;

    public ArcOrchestration(@NotNull ManagedComponentContext context, @NotNull Arc arc) {
        super(context, Label.of(arc.workload().name()).with("Orchestration"));

        Pass head = Pass.Builder.create(this, "Start")
                .inputPath("$.detail.responsePayload.event") // "$.detail.event"
                .resultPath("$.inputKey.event")
                .build();

        head.next(succeed("Success"));

        stateMachineName = Workloads.workloadBaseName(context().deployable(), arc);

        LogGroup logGroup = LogGroup.Builder.create(this, Label.of("LogGroup").with(stateMachineName).camelCase())
//                .logGroupName("/aws/lambda/" + transformEventFunction.getFunctionName())
                .removalPolicy(RemovalPolicy.DESTROY)
                .retention(RetentionDays.ONE_DAY)
                .build();

        LogOptions logOptions = LogOptions.builder()
                .destination(logGroup)
                .level(LogLevel.ALL)
                .build();

        stateMachine = StateMachine.Builder.create(this, stateMachineName.camelCase())
                .stateMachineName(stateMachineName.lowerHyphen())
                .stateMachineType(StateMachineType.STANDARD)
                .logs(logOptions)
                .definition(head)
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
