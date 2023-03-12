/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc;

import clusterless.lambda.arc.ArcStateProps;
import clusterless.managed.component.ArcComponent;
import clusterless.model.deploy.Arc;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.substrate.aws.resources.Arcs;
import clusterless.substrate.aws.uri.ArcURI;
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
    private final ArcStateProps arcStateProps;
    private final LambdaJavaRuntimeProps runtimeProps;
    private StateMachine stateMachine;

    public ArcOrchestration(@NotNull ManagedComponentContext context, @NotNull Arc<?> arc) {
        super(context, Label.of(arc.name()).with("Orchestration"));

        stateMachineName = Arcs.arcBaseName(context().deployable(), arc);

        arcStateProps = ArcStateProps.builder()
                .withName(arc.name())
                .withProject(context().deployable().project())
                .withSinks(arc.sinks())
                .withSources(arc.sources())
                .withArcStatePath(
                        ArcURI.builder()
                                .withPlacement(context().deployable().placement())
                                .withProject(context().deployable().project())
                                .withArcName(arc.name())
                                .build()
                )
                .build();

        runtimeProps = LambdaJavaRuntimeProps.builder()
                .withTimeoutMin(5)
                .build();
    }

    public void buildOrchestrationWith(ArcComponent arcComponent) {
        IChainable head = Pass.Builder.create(this, "Start")
                .outputPath("$.detail")
                .build();

        Chain next = Chain.start(head);

        ArcStartStateGate startStateGate = new ArcStartStateGate(context(), arcStateProps, runtimeProps);

//        next.next(startStateGate.createState())
//                .next(Choice.Builder.create(this, "StateStart")
//                        .build()
//                        .when(Condition.stringEquals("","")))
//
//                .next(((ArcConstruct<?>) arcComponent).createState())
//                .next(succeed("Success"));

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
