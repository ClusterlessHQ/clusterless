/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc.s3copy;

import clusterless.cls.substrate.aws.arc.common.WorkloadManagedConstruct;
import clusterless.cls.substrate.aws.arc.props.ArcEnvBuilder;
import clusterless.cls.substrate.aws.construct.ArcConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.commons.naming.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.stepfunctions.IChainable;
import software.amazon.awscdk.services.stepfunctions.TaskStateBase;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;

import java.util.Map;
import java.util.function.Consumer;

/**
 *
 */
public class S3CopyArcConstruct extends ArcConstruct<S3CopyArc> {
    private final Label baseId = Label.of("Func");
    private final String handler = "clusterless.aws.lambda.workload.s3copy.S3CopyArcEventHandler";
    private final Function function;

    public S3CopyArcConstruct(@NotNull ManagedComponentContext context, @NotNull S3CopyArc model) {
        super(context, model);

        Map<String, String> environment = new ArcEnvBuilder(placement(), model()).asEnvironment();

        Label modelName = Label.of(model().name());
        LambdaJavaRuntimeProps lambdaJavaRuntimeProps = model()
                .workload()
                .runtimeProps();

        function = new WorkloadManagedConstruct(context, baseId, modelName, handler, lambdaJavaRuntimeProps, environment)
                .function();

        grantManifestAndDatasetPermissionsTo(function());
    }

    public Function function() {
        return function;
    }

    @Override
    public IChainable createState(String inputPath, String resultPath, IChainable failed, Consumer<TaskStateBase> taskAmendments) {
        LambdaInvoke invoke = LambdaInvoke.Builder.create(this, "S3CopyFunction")
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
