/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.s3copy;

import clusterless.lambda.workload.s3copy.S3CopyArcEventHandler;
import clusterless.naming.Label;
import clusterless.substrate.aws.arc.props.ArcEnvBuilder;
import clusterless.substrate.aws.construct.ArcConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.props.Lookup;
import clusterless.substrate.aws.resources.Assets;
import clusterless.substrate.aws.resources.Functions;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.stepfunctions.IChainable;
import software.amazon.awscdk.services.stepfunctions.TaskStateBase;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;

import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 *
 */
public class S3CopyArcConstruct extends ArcConstruct<S3CopyArc> {
    private final Function function;

    public S3CopyArcConstruct(@NotNull ManagedComponentContext context, @NotNull S3CopyArc model) {
        super(context, model);

        Map<String, String> environment = new ArcEnvBuilder(placement(), model()).asEnvironment();

        String functionName = Functions.functionName(this, model().name(), "S3Copy");
        Label functionLabel = Label.of(model().name()).with("S3Copy");
        function = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionName)
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-workload.*\\.zip$"))) // get packaged code
                .architecture(Lookup.architecture(model().workload().runtimeProps().architecture()))
                .handler(S3CopyArcEventHandler.class.getName()) // get handler class name
                .environment(environment)
                .runtime(Runtime.JAVA_11)
                .memorySize(model().workload().runtimeProps().memorySizeMB())
                .timeout(Duration.minutes(model().workload().runtimeProps().timeoutMin()))
                .build();

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
