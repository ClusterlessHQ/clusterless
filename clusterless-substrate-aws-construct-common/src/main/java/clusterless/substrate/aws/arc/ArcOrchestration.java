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
import clusterless.naming.Label;
import clusterless.substrate.aws.arc.state.ArcCompleteStateGate;
import clusterless.substrate.aws.arc.state.ArcStartStateGate;
import clusterless.substrate.aws.construct.ArcConstruct;
import clusterless.substrate.aws.event.ArcStateContext;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.substrate.aws.resources.Arcs;
import clusterless.substrate.aws.resources.Events;
import clusterless.substrate.uri.ArcURI;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.events.targets.SfnStateMachine;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.stepfunctions.*;

import java.util.List;

import static clusterless.substrate.aws.event.ArcStateContext.SINK_MANIFESTS_PATH;
import static clusterless.substrate.aws.event.ArcStateContext.WORKLOAD_CONTEXT_PATH;

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
        ArcCompleteStateGate completeStateGate = new ArcCompleteStateGate(context(), arc, arcStateProps, runtimeProps);

        Chain rootChain = Chain.start(pass("CurrentStateChoice", "$.detail"))
                // confirm the state is valid and forward to the workload
                .next(new ArcStartStateGate(context(), arc, arcStateProps, runtimeProps))
                .next(
                        startGateResultChoice(
                                Chain.start(stateFrom(arcComponent, completeStateGate))
                                        // publish new events on complete of workload
                                        .next(completeStateGate)
                                        .next(completeGateResultChoice())
                        )
                );

        stateMachine = StateMachine.Builder.create(this, stateMachineName.camelCase())
                .stateMachineName(stateMachineName.lowerHyphen())
                .stateMachineType(StateMachineType.STANDARD)
                .logs(createLogOptions())
                .definitionBody(DefinitionBody.fromChainable(rootChain))
                .build();
    }

    private IChainable stateFrom(ArcComponent arcComponent, IChainable fail) {
        // TODO: see if we can simplify the createState api by using a Pass
        //       before and after to handle the inputPath and outputPath
        return ((ArcConstruct<?>) arcComponent)
                .createState(
                        WORKLOAD_CONTEXT_PATH,
                        SINK_MANIFESTS_PATH,
                        fail,
                        taskStateBase -> taskStateBase.addCatch(
                                Pass.Builder.create(this, taskStateBase.getId() + "Catch")
                                        .comment("Task threw an error")
                                        .build()
                                        .next(fail),
                                CatchProps.builder()
                                        .resultPath(ArcStateContext.ERROR_PATH)
                                        .errors(List.of(Errors.ALL))
                                        .build()
                        )
                );
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
                        fail("UnknownStart", "Unknown arc state encountered.")
                );
    }

    @NotNull
    private Choice completeGateResultChoice() {
        return Choice.Builder.create(this, "StateComplete")
                .build()
                // "Error"
                .when(
                        Condition.and(
                                Condition.isPresent("$.workloadError.Error"),
                                Condition.stringEquals("$.currentState", ArcState.partial.name())
                        ),
                        fail("PartialWithError", "Only partial data was created, due to an error, see arc state")
                ).when(
                        Condition.and(
                                Condition.isPresent("$.workloadError.Error"),
                                Condition.stringEquals("$.currentState", ArcState.missing.name())
                        ),
                        fail("MissingWithError", "No data was created, due to an error, see arc state")
                ).when(
                        Condition.and(
                                Condition.isPresent("$.workloadError.Error")
                        ),
                        fail("UnknownError", "Unknown error state")
                ).when(
                        Condition.and(
                                Condition.isPresent("$.currentState"),
                                Condition.stringEquals("$.currentState", ArcState.partial.name())
                        ),
                        succeed("Partial", "Only partial data was created, possibly due to an internal error")
                ).when(
                        Condition.and(
                                Condition.isPresent("$.currentState"),
                                Condition.stringEquals("$.currentState", ArcState.missing.name())
                        ),
                        succeed("Missing", "No data was created, possibly due to an error")
                ).when(
                        Condition.and(
                                Condition.isPresent("$.currentState"),
                                Condition.stringEquals("$.currentState", ArcState.complete.name())
                        ),
                        succeed("Complete", "Workload completed.")
                ).otherwise(
                        fail("UnknownComplete", "Unknown arc state encountered")
                );
    }

    @NotNull
    private LogOptions createLogOptions() {
        String baseId = Label.of("LogGroup")
                .with(stateMachineName)
                .camelCase();

        LogGroup logGroup = LogGroup.Builder.create(this, baseId)
                .logGroupName("/aws/vendedlogs/states/" + stateMachineName.lowerHyphen())
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
