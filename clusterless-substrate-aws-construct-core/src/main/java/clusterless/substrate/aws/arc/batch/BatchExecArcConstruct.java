/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.batch;

import clusterless.substrate.aws.arc.props.ArcEnvBuilder;
import clusterless.substrate.aws.construct.ArcConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.substrate.aws.resources.Refs;
import clusterless.substrate.aws.resources.Resources;
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

/**
 * https://stackoverflow.com/questions/65835855/aws-batch-job-execution-results-in-step-function
 */
public class BatchExecArcConstruct extends ArcConstruct<BatchExecArc> {
    private static final Logger LOG = LogManager.getLogger(BatchExecArcConstruct.class);
    private final EcsJobDefinition jobDefinition;
    private final JobQueue jobQueue;

    public BatchExecArcConstruct(@NotNull ManagedComponentContext context, @NotNull BatchExecArc model) {
        super(context, model);

        Map<String, String> environment = new ArcEnvBuilder(placement(), model()).asEnvironment();

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
                .command(List.of()) // TODO:
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
    public State createState(String inputPath, String resultPath, State failed) {

        BatchSubmitJob batchSubmitTask = BatchSubmitJob.Builder.create(this, "BatchSubmit")
                .jobName("name")
                .jobQueueArn(jobQueue.getJobQueueArn())
                .resultPath(resultPath)
                .jobDefinitionArn(jobDefinition.getJobDefinitionArn())
//                .payload(TaskInput.fromJsonPathAt())
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
