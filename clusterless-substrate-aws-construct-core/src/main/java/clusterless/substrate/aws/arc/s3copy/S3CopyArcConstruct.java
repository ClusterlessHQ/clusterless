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
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.construct.ArcConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.resources.Assets;
import clusterless.substrate.aws.resources.Buckets;
import clusterless.util.Env;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.stepfunctions.State;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;

import java.net.URI;
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

        Map<String, URI> sourceManifestPaths = model.sources()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Buckets.manifestPath(this, ManifestState.complete, e.getValue())));

        Map<String, URI> sinkManifestCompletePaths = model.sinks()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Buckets.manifestPath(this, ManifestState.complete, e.getValue())));

        Map<String, URI> sinkManifestRollbackPaths = model.sinks()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Buckets.manifestPath(this, ManifestState.partial, e.getValue())));

        ArcProps arcProps = ArcProps.Builder.builder()
                .withSourceManifestPaths(sourceManifestPaths)
                .withSinkManifestCompletePaths(sinkManifestCompletePaths)
                .withSinkManifestRollbackPaths(sinkManifestRollbackPaths)
                .build();

        Map<String, String> environment = Env.toEnv(arcProps);

        Label functionLabel = Label.of(model().name()).with("S3CopyArc");
        function = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionLabel.lowerHyphen())
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-workload.*\\.zip$"))) // get packaged code
                .handler(S3CopyArcEventHandler.class.getName()) // get handler class name
                .environment(environment)
                .runtime(Runtime.JAVA_11)
                .memorySize(model().workload().runtimeProps().memorySizeMB())
                .timeout(Duration.minutes(model().workload().runtimeProps().timeoutMin()))
                .build();
    }

    public Function function() {
        return function;
    }

    @Override
    public State createState() {
        return LambdaInvoke.Builder.create(this, "S3CopyFunction")
                .lambdaFunction(function())
                .payloadResponseOnly(true) // sets .invocationType(LambdaInvocationType.REQUEST_RESPONSE)
                .build();
    }
}
