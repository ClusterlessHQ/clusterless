/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc.glue;

import clusterless.cls.naming.Label;
import clusterless.cls.substrate.aws.arc.common.WorkloadManagedConstruct;
import clusterless.cls.substrate.aws.arc.props.ArcEnvBuilder;
import clusterless.cls.substrate.aws.construct.ArcConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.cls.substrate.aws.resources.Databases;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.stepfunctions.IChainable;
import software.amazon.awscdk.services.stepfunctions.TaskStateBase;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public class GlueAddPartitionsArcConstruct extends ArcConstruct<GlueAddPartitionsArc> {
    private final Label baseId = Label.of("Func");
    private final String handler = "clusterless.aws.lambda.workload.glue.GlueAddPartitionsArcEventHandler";
    private final Function function;

    public GlueAddPartitionsArcConstruct(@NotNull ManagedComponentContext context, @NotNull GlueAddPartitionsArc model) {
        super(context, model);

        Map<String, String> environment = new ArcEnvBuilder(placement(), model())
                .asEnvironment();

        Label modelName = Label.of(model().name());
        LambdaJavaRuntimeProps lambdaJavaRuntimeProps = model()
                .workload()
                .runtimeProps();

        function = new WorkloadManagedConstruct(context, baseId, modelName, handler, lambdaJavaRuntimeProps, environment)
                .function();

        grantCreatePartition();
        grantManifestAndDatasetPermissionsTo(function());
    }

    public Function function() {
        return function;
    }

    protected void grantCreatePartition() {
        List<String> actions = List.of(
                "glue:CreatePartition",
                "glue:BatchCreatePartition",
                "glue:GetTable"
        );
        applyToEachTable(
                model().sinks(),
                u -> function.addToRolePolicy(applyTablePolicy(u, actions, Effect.ALLOW))
        );
    }

    protected PolicyStatement applyTablePolicy(URI glueUri, List<String> actions, Effect effect) {
        return PolicyStatement.Builder.create()
                .actions(actions)
                .effect(effect)
                .resources(List.of(
                                Databases.catalogARNFrom(this, glueUri),
                                Databases.databaseARNFrom(this, glueUri),
                                Databases.tableARNFrom(this, glueUri)
                        )
                )
                .build();
    }

    @Override
    public IChainable createState(String inputPath, String resultPath, IChainable failed, Consumer<TaskStateBase> taskAmendments) {
        LambdaInvoke invoke = LambdaInvoke.Builder.create(this, "AddPartitionsFunction")
                .lambdaFunction(function())
                .retryOnServiceExceptions(true)
                .payloadResponseOnly(true) // sets .invocationType(LambdaInvocationType.REQUEST_RESPONSE)
                .inputPath(inputPath)
                .resultPath(resultPath)
                .build();

        taskAmendments.accept(invoke);

        return invoke;
    }
}
