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
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.substrate.aws.resources.Refs;
import clusterless.substrate.aws.resources.Resources;
import clusterless.substrate.aws.resources.StateURIs;
import clusterless.substrate.aws.uri.ManifestURI;
import clusterless.util.Env;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Size;
import software.amazon.awscdk.services.batch.alpha.*;
import software.amazon.awscdk.services.ecr.assets.Platform;
import software.amazon.awscdk.services.ecs.AssetImage;
import software.amazon.awscdk.services.ecs.AssetImageProps;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.stepfunctions.CatchProps;
import software.amazon.awscdk.services.stepfunctions.Errors;
import software.amazon.awscdk.services.stepfunctions.State;
import software.amazon.awscdk.services.stepfunctions.tasks.BatchSubmitJob;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
public class BatchExecArcConstruct extends ArcConstruct<BatchExecArc> {
    private static final Logger LOG = LogManager.getLogger(BatchExecArcConstruct.class);
    private final EcsJobDefinition jobDefinition;
    private final JobQueue jobQueue;

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

        AssetImage image = ContainerImage.fromAsset(
                model().workload().imagePath().toString(),
                AssetImageProps.builder()
//                        .networkMode(NetworkMode.HOST)
                        .platform(Platform.LINUX_AMD64)
                        .build()
        );

        Role executionRole = Role.Builder.create(this, "ExecutionRole")
                .assumedBy(ServicePrincipal.Builder.create("ecs-tasks.amazonaws.com").build())
                .build();

        // https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-cpu-memory-error.html
        EcsFargateContainerDefinition container = EcsFargateContainerDefinition.Builder.create(this, "FargateContainer")
                .image(image)
                .executionRole(executionRole)
                .environment(environment)
                .command(List.of())
                .cpu(.25) // vcpu - 1 vCPU is equivalent to 1,024 CPU
                .memory(Size.gibibytes(1))
                .build();

        jobDefinition = EcsJobDefinition.Builder.create(this, "JobDef")
                .jobDefinitionName(Resources.regionallyUniqueProjectName(this, model.name())) // physical name
//                .parameters()
                .container(container)
//                .retryAttempts(1)
                .timeout(Duration.hours(1))
                .build();

        IManagedComputeEnvironment computeEnvironment = resolveComputeEnvironment(model().workload().computeEnvironmentRef());

        jobQueue = JobQueue.Builder.create(this, "JobQueue")
                .jobQueueName(Resources.regionallyUniqueProjectName(this, model.name()))
                .build();

        jobQueue.addComputeEnvironment(computeEnvironment, 1);

        grantPermissionsTo(executionRole);
    }

    @NotNull
    protected IManagedComputeEnvironment resolveComputeEnvironment(String computeEnvironmentRef) {
        Construct construct = StagedApp.stagedOf(this).resolveRef(computeEnvironmentRef);

        if (construct != null) {
            return (IManagedComputeEnvironment) construct;
        }

        Optional<String> computeEnvironmentArn = Refs.resolveArn(this, computeEnvironmentRef);

        LOG.info("using computeEnvironment arn: {}", computeEnvironmentArn);

        return FargateComputeEnvironment.fromFargateComputeEnvironmentArn(
                this,
                "ComputeEnvironment",
                computeEnvironmentArn.orElseThrow(() -> new IllegalStateException("computeEnvironment ref or arn are required"))
        );
    }

    @Override
    public State createState(String resultPath, State failed) {

        BatchSubmitJob batchSubmitTask = BatchSubmitJob.Builder.create(this, "BatchSubmit")
                .jobName("name")
                .jobQueueArn(jobQueue.getJobQueueArn())
                .resultPath(resultPath)
                .jobDefinitionArn(jobDefinition.getJobDefinitionArn())
//                .payload(resultPath)
                .build();

        batchSubmitTask.addCatch(
                failed,
                CatchProps.builder()
                        .errors(List.of(Errors.ALL))
                        .build()
        );

        return batchSubmitTask;
    }
}
