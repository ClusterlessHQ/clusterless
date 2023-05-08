/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.batch;

import clusterless.lambda.arc.ArcProps;
import clusterless.model.deploy.WorkloadProps;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.construct.ArcConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.resources.StateURIs;
import clusterless.substrate.aws.uri.ManifestURI;
import clusterless.util.Env;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.stepfunctions.State;

import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class BatchExecArcConstruct extends ArcConstruct<BatchExecArc> {

    public BatchExecArcConstruct(@NotNull ManagedComponentContext context, @NotNull BatchExecArc model) {
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

    }

    @Override
    public State createState(String resultPath, State failed) {
//        LambdaInvoke invoke = LambdaInvoke.Builder.create(this, "S3CopyFunction")
//                .lambdaFunction(function())
//                .retryOnServiceExceptions(true)
//                .payloadResponseOnly(true) // sets .invocationType(LambdaInvocationType.REQUEST_RESPONSE)
//                .resultPath(resultPath)
//                .build();
//
//        invoke.addCatch(
//                failed,
//                CatchProps.builder()
//                        .errors(List.of(Errors.ALL))
//                        .build()
//        );

//        return invoke;
        return null;
    }
}
