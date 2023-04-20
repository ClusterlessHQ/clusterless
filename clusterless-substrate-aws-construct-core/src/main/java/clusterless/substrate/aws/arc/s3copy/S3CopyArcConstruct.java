/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.s3copy;

import clusterless.lambda.arc.ArcProps;
import clusterless.lambda.workload.s3copy.S3CopyArcEventHandler;
import clusterless.model.deploy.WorkloadProps;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.construct.ArcConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.resources.Assets;
import clusterless.substrate.aws.resources.Functions;
import clusterless.substrate.aws.resources.StateURIs;
import clusterless.substrate.aws.uri.ManifestURI;
import clusterless.util.Env;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.stepfunctions.CatchProps;
import software.amazon.awscdk.services.stepfunctions.Errors;
import software.amazon.awscdk.services.stepfunctions.State;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
public class S3CopyArcConstruct extends ArcConstruct<S3CopyArc> {

    private final Function function;

    public S3CopyArcConstruct(@NotNull ManagedComponentContext context, @NotNull S3CopyArc model) {
        super(context, model);

        Map<String, ManifestURI> sourceManifestPaths = model.sources()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> StateURIs.manifestPath(this, ManifestState.complete, e.getValue())));

        Map<String, ManifestURI> sinkManifestPaths = model.sinks()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> StateURIs.manifestPath(this, e.getValue())));

        ArcProps<WorkloadProps> arcProps = ArcProps.builder()
                .withSources(model().sources())
                .withSinks(model().sinks())
                .withSourceManifestPaths(sourceManifestPaths)
                .withSinkManifestPaths(sinkManifestPaths)
                .withWorkloadProps(model.workload().workloadProps())
                .build();

        Map<String, String> environment = Env.toEnv(arcProps);

        String functionName = Functions.functionName(this, model().name(), "S3Copy");
        Label functionLabel = Label.of(model().name()).with("S3Copy");
        function = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionName)
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-workload.*\\.zip$"))) // get packaged code
                .handler(S3CopyArcEventHandler.class.getName()) // get handler class name
                .environment(environment)
                .runtime(Runtime.JAVA_11)
                .memorySize(model().workload().runtimeProps().memorySizeMB())
                .timeout(Duration.minutes(model().workload().runtimeProps().timeoutMin()))
                .build();

        grantPermissionsTo(function());
    }

    public Function function() {
        return function;
    }

    @Override
    public State createState(String resultPath, State failed) {
        LambdaInvoke invoke = LambdaInvoke.Builder.create(this, "S3CopyFunction")
                .lambdaFunction(function())
                .retryOnServiceExceptions(true)
                .payloadResponseOnly(true) // sets .invocationType(LambdaInvocationType.REQUEST_RESPONSE)
                .resultPath(resultPath)
                .build();

        invoke.addCatch(
                failed,
                CatchProps.builder()
                        .errors(List.of(Errors.ALL))
                        .build()
        );

        return invoke;
    }
}
