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
import clusterless.model.state.ArcState;
import clusterless.substrate.aws.arc.state.ArcCompleteStateGate;
import clusterless.substrate.aws.arc.state.ArcStartStateGate;
import clusterless.substrate.aws.construct.ArcConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.substrate.aws.resources.Arcs;
import clusterless.substrate.aws.resources.Events;
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
    private final Arc<?> arc;
    private final ArcStateProps arcStateProps;
    private final LambdaJavaRuntimeProps runtimeProps;
    private StateMachine stateMachine;

    public ArcOrchestration(@NotNull ManagedComponentContext context, @NotNull Arc<?> arc) {
        super(context, Label.of(arc.name()).with("Orchestration"));
        this.arc = arc;

        this.stateMachineName = Arcs.arcBaseName(context().deployable(), arc);

        this.arcStateProps = ArcStateProps.builder()
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
                .withEventBusName(Events.arcEventBusNameRef(this))
                .build();

        this.runtimeProps = LambdaJavaRuntimeProps.builder()
                .withTimeoutMin(5)
                .build();
    }

    public void buildOrchestrationWith(ArcComponent arcComponent) {
        Chain rootChain = Chain.start(pass("CurrentStateChoice", "$.detail"))
                // confirm the state is valid and forward to the workload
                .next(new ArcStartStateGate(context(), arc, arcStateProps, runtimeProps))
                .next(
                        startGateResultChoice(
                                Chain.start(stateFrom(arcComponent, fail("Failed", "Failed with unhandled error")))
                                        // publish new events on complete of workload
                                        .next(new ArcCompleteStateGate(context(), arc, arcStateProps, runtimeProps))
                                        .next(succeed("Complete"))
                        )
                );

        stateMachine = StateMachine.Builder.create(this, stateMachineName.camelCase())
                .stateMachineName(stateMachineName.lowerHyphen())
                .stateMachineType(StateMachineType.STANDARD)
                .logs(createLogOptions())
                .definition(rootChain)
                .build();
    }

    private State stateFrom(ArcComponent arcComponent, Fail fail) {
        return ((ArcConstruct<?>) arcComponent)
                .createState("$.sinkManifests", fail);
    }

    @NotNull
    private Choice startGateResultChoice(Chain workloadChain) {
        return Choice.Builder.create(this, "StateStart")
                .build()
                .when(
                        Condition.and(
                                Condition.isPresent("$.previousState"),
                                Condition.stringEquals("$.previousState", ArcState.running.name())
                        ),
                        succeed("AlreadyRunning", "Workload is already running, skipping.")
                ).when(
                        Condition.and(
                                Condition.isPresent("$.currentState"),
                                Condition.or(
                                        Condition.stringEquals("$.currentState", ArcState.complete.name()),
                                        Condition.stringEquals("$.currentState", ArcState.missing.name())
                                )
                        ),
                        succeed("AlreadyComplete", "Workload already completed.")
                ).when(
                        Condition.and(
                                Condition.isPresent("$.currentState"),
                                Condition.stringEquals("$.currentState", ArcState.running.name())
                        ),
                        workloadChain
                ).otherwise(
                        fail("UnknownState", "Unknown arc state encountered")
                );
    }

    @NotNull
    private LogOptions createLogOptions() {
        String baseId = Label.of("LogGroup")
                .with(stateMachineName)
                .camelCase();

        LogGroup logGroup = LogGroup.Builder.create(this, baseId)
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
