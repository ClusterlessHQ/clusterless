/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc.batch;

import clusterless.cls.substrate.aws.arc.common.WorkloadManagedConstruct;
import clusterless.cls.substrate.aws.arc.props.ArcEnvBuilder;
import clusterless.cls.substrate.aws.construct.ArcConstruct;
import clusterless.cls.substrate.aws.event.ArcStateContext;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.cls.substrate.aws.props.Lookup;
import clusterless.cls.substrate.aws.resources.Policies;
import clusterless.commons.naming.Label;
import clusterless.commons.substrate.aws.cdk.naming.ResourceNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Size;
import software.amazon.awscdk.services.batch.*;
import software.amazon.awscdk.services.ecr.assets.NetworkMode;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.stepfunctions.*;
import software.amazon.awscdk.services.stepfunctions.tasks.BatchSubmitJob;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BatchExecArcConstruct extends ArcConstruct<BatchExecArc> {
    private static final Logger LOG = LogManager.getLogger(BatchExecArcConstruct.class);
    private final Label regionalName;
    private final RetentionDays retentionDays = RetentionDays.ONE_DAY;
    private final RemovalPolicy removalPolicy = RemovalPolicy.DESTROY;
    private final EcsJobDefinition jobDefinition;
    private final JobQueue jobQueue;
    private final BatchPayloadCommand payloadCommand;
    private final Function function;

    public BatchExecArcConstruct(@NotNull ManagedComponentContext context, @NotNull BatchExecArc model) {
        super(context, model);

        Label modelName = Label.of(model.name());

        regionalName = ResourceNames.regionUniqueScopedLabel(this, modelName);

        Map<String, String> environment = new ArcEnvBuilder(placement(), model())
                .asEnvironment();

        AssetImage image = ContainerImage.fromAsset(
                model().workload().imagePath().toString(),
                AssetImageProps.builder()
                        .networkMode(NetworkMode.HOST)
                        .platform(Lookup.platform(model().workload().batchRuntimeProps().architecture()))
                        .build()
        );

        payloadCommand = new BatchPayloadCommand(model().workload().command());

        LogGroup logGroup = LogGroup.Builder.create(this, Label.of("LogGroup").with(model().name()).camelCase())
                .logGroupName("/aws/batch/" + regionalName.lowerHyphen()) // part of the ARN
                .removalPolicy(removalPolicy)
                .retention(retentionDays)
                .build();

        IRole jobRole = Role.Builder.create(this, "JobRole")
                .assumedBy(ServicePrincipal.Builder.create("ecs-tasks.amazonaws.com").build())
                .build();

        jobRole.addToPrincipalPolicy(Policies.createCloudWatchPolicyStatement()); // allow workload to push metrics
        grantManifestAndDatasetPermissionsTo(jobRole);

        // https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-cpu-memory-error.html
        EcsFargateContainerDefinition container = EcsFargateContainerDefinition.Builder.create(this, "FargateContainer")
                .image(image)
                .jobRole(jobRole)
                .assignPublicIp(true)
                .logging(LogDrivers.awsLogs(AwsLogDriverProps.builder()
                        // prefix to the log stream name
                        // use to designate the container execution type (Fargate, EC2, etc)
                        .streamPrefix(Label.of("Fargate").lowerHyphen())
                        .logGroup(logGroup)
                        .build()))
                .environment(environment)
                .command(payloadCommand.command())
                .cpu(.25) // vcpu - 1 vCPU is equivalent to 1,024 CPU
                .memory(Size.mebibytes(model().workload().batchRuntimeProps().memorySizeMB()))
                .build();

        jobDefinition = EcsJobDefinition.Builder.create(this, "JobDef")
                .jobDefinitionName(regionalName.lowerHyphen()) // physical name
                .container(container)
                .retryAttempts(model().workload().batchRuntimeProps().retryAttempts())
                .timeout(Duration.minutes(model().workload().batchRuntimeProps().timeoutMin()))
                .build();

        IManagedComputeEnvironment computeEnvironment = resolveComputeEnvironment(model().workload().computeEnvironmentRef());

        jobQueue = JobQueue.Builder.create(this, "JobQueue")
                .jobQueueName(regionalName.lowerHyphen())
                .build();

        jobQueue.addComputeEnvironment(computeEnvironment, 1);

        String handler = "clusterless.aws.lambda.workload.batch.BatchResultHandler";

        Label baseId = Label.of("Result");
        LambdaJavaRuntimeProps lambdaJavaRuntimeProps = model()
                .workload()
                .lambdaRuntimeProps();

        function = new WorkloadManagedConstruct(context, baseId, modelName, handler, lambdaJavaRuntimeProps, environment)
                .function();

        grantManifestRead(function());
    }

    public Function function() {
        return function;
    }

    @NotNull
    protected IManagedComputeEnvironment resolveComputeEnvironment(String computeEnvironmentRef) {

        LOG.info("resolving computeEnvironment ref: {}", computeEnvironmentRef);

        return resolveArnRef(computeEnvironmentRef, arn -> {
            LOG.info("using computeEnvironment arn: {}", arn);
            return FargateComputeEnvironment.fromFargateComputeEnvironmentArn(
                    this,
                    "ComputeEnvironment",
                    arn);
        });
    }

    @Override
    public IChainable createState(String inputPath, String resultPath, IChainable failed, Consumer<TaskStateBase> taskAmendments) {
        TaskInput payload = TaskInput.fromObject(
                payloadCommand.payload()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> JsonPath.stringAt(e.getValue())))
        );

        BatchSubmitJob batchSubmitTask = BatchSubmitJob.Builder.create(this, "BatchSubmit")
                .jobName(regionalName.camelCase())
                .jobQueueArn(jobQueue.getJobQueueArn())
                .inputPath(inputPath)
                .resultPath(ArcStateContext.RESPONSE_PATH) // put the result on the ArcStateContext
                .jobDefinitionArn(jobDefinition.getJobDefinitionArn())
                .integrationPattern(IntegrationPattern.RUN_JOB)
                .payload(payload)
                .build();

        taskAmendments.accept(batchSubmitTask);

        // add lambda to scrape manifests
        LambdaInvoke invoke = LambdaInvoke.Builder.create(this, "BatchResults")
                .lambdaFunction(function())
                .retryOnServiceExceptions(true)
                .payloadResponseOnly(true) // sets .invocationType(LambdaInvocationType.REQUEST_RESPONSE)
                .inputPath(inputPath)
                .resultPath(resultPath)
                .build();

        taskAmendments.accept(invoke);

        return Chain.start(batchSubmitTask)
                .next(invoke);
    }
}
