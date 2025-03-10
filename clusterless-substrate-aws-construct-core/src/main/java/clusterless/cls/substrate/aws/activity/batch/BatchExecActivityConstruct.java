/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.activity.batch;

import clusterless.cls.substrate.aws.common.batch.BatchPayloadCommand;
import clusterless.cls.substrate.aws.construct.ActivityConstruct;
import clusterless.cls.substrate.aws.construct.IsScheduled;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.props.Lookup;
import clusterless.cls.substrate.aws.resource.s3.S3BucketResourceConstruct;
import clusterless.cls.substrate.aws.resources.Policies;
import clusterless.cls.util.Env;
import clusterless.cls.util.URIs;
import clusterless.commons.naming.Label;
import clusterless.commons.substrate.aws.cdk.naming.ResourceNames;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Size;
import software.amazon.awscdk.services.batch.*;
import software.amazon.awscdk.services.ecr.assets.NetworkMode;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.events.targets.BatchJob;
import software.amazon.awscdk.services.iam.Grant;
import software.amazon.awscdk.services.iam.IRole;
import software.amazon.awscdk.services.iam.Role;
import software.amazon.awscdk.services.iam.ServicePrincipal;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;

import java.net.URI;
import java.util.Map;

/**
 * Environment Variables:
 * AWS_BATCH_JOB_ATTEMPT=1
 * HOSTNAME=ip-....us-west-2.compute.internal
 * AWS_CONTAINER_CREDENTIALS_RELATIVE_URI=/v2/credentials/5...7
 * CLS_ACTIVITY_PROPS_JSON={"schedule":"...","pathURI":"s3://..."}
 * CLS_ACTIVITY_PROPS_JAVA={"__type":"clusterless.cls.substrate.aws.activity.batch.ActivityProps","schedule":"...","pathURI":"s3://..."}
 * AWS_EXECUTION_ENV=AWS_ECS_FARGATE
 * PATH=/bin:/usr/bin:/sbin:/usr/sbin:/usr/local/bin:/usr/local/sbin
 * AWS_DEFAULT_REGION=us-west-2
 * PWD=/app
 * ECS_AGENT_URI=http://.../api/9493...ad-2470140894
 * AWS_REGION=us-west-2
 * AWS_BATCH_JOB_ID=a7182d0e-7e9e-44a1-8417-3b7d8dac9a42
 * SHLVL=1
 * HOME=/root
 * AWS_BATCH_CE_NAME=DEV-da-oclic-ingress-tx-simple-compute-environment-20250101
 * ECS_CONTAINER_METADATA_URI=http://.../v3/9493...ad-2470140894
 * ECS_CONTAINER_METADATA_URI_V4=http://.../v4/9493...ad-2470140894
 * AWS_BATCH_JQ_NAME=DEV-...
 */
public class BatchExecActivityConstruct extends ActivityConstruct<BatchExecActivity> implements IsScheduled {
    private static final Logger LOG = LoggerFactory.getLogger(BatchExecActivityConstruct.class);
    private final Label regionalName;
    private final RetentionDays retentionDays = RetentionDays.ONE_DAY;
    private final RemovalPolicy removalPolicy = RemovalPolicy.DESTROY;
    private final EcsJobDefinition jobDefinition;
    private final JobQueue jobQueue;
    private final BatchPayloadCommand payloadCommand;

    public BatchExecActivityConstruct(ManagedComponentContext context, BatchExecActivity model) {
        super(context, model);

        Label modelName = Label.of(model.name());

        String bucketRef = model.bucketRef();
        URI pathURI = model.pathURI();

        IBucket destinationBucket;
        if (bucketRef != null) {
            destinationBucket = resolveLocalConstruct(bucketRef);

            S3BucketResourceConstruct scope = (S3BucketResourceConstruct) destinationBucket.getNode().getScope();
            String bucketName = scope != null ? scope.model().bucketName() : null;

            if (bucketName == null) {
                throw new IllegalStateException("failed to resolve bucket name for: " + bucketRef);
            }

            pathURI = URIs.create("s3", bucketName, pathURI.getPath());

        } else {
            LOG.warn("no bucketRef specified, using: {}, destination may need policy statement to allow 's3:GetBucketAcl'", pathURI);
            destinationBucket = Bucket.fromBucketName(this, "DestinationBucket", pathURI.getHost());
        }

        regionalName = ResourceNames.regionUniqueScopedLabel(this, modelName);

        ActivityProps activityProps = ActivityProps.Builder.builder()
                .withSchedule(model().schedule()) // normalize the unit name
                .withPathURI(pathURI)
                .build();

        Map<String, String> environment = Env.toEnv(activityProps);

        environment.putAll(model().environment());

        AssetImage image = ContainerImage.fromAsset(
                model().imagePath().toString(),
                AssetImageProps.builder()
                        .networkMode(NetworkMode.HOST)
                        .buildArgs(model().imageBuildArgs())
                        .platform(Lookup.platform(model().batchRuntimeProps().architecture()))
                        .build()
        );

        payloadCommand = new BatchPayloadCommand(model().command());

        LogGroup logGroup = LogGroup.Builder.create(this, Label.of("LogGroup").with(model().name()).camelCase())
                .logGroupName("/aws/batch/" + regionalName.lowerHyphen()) // part of the ARN
                .removalPolicy(removalPolicy)
                .retention(retentionDays)
                .build();

        IRole jobRole = Role.Builder.create(this, "JobRole")
                .assumedBy(ServicePrincipal.Builder.create("ecs-tasks.amazonaws.com").build())
                .build();

        jobRole.addToPrincipalPolicy(Policies.createCloudWatchPolicyStatement()); // allow workload to push metrics

        Grant grant = destinationBucket
                .grantReadWrite(jobRole);

        grant.assertSuccess();

        // https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-cpu-memory-error.html
        EcsFargateContainerDefinition container = EcsFargateContainerDefinition.Builder.create(this, "FargateContainer")
                .image(image)
                .jobRole(jobRole)
                .assignPublicIp(true) // TODO: parameterize this with default false
                .logging(LogDrivers.awsLogs(AwsLogDriverProps.builder()
                        // prefix to the log stream name
                        // use to designate the container execution type (Fargate, EC2, etc)
                        .streamPrefix(Label.of("Fargate").lowerHyphen())
                        .logGroup(logGroup)
                        .build()))
                .environment(environment)
                .command(payloadCommand.command())
                .cpu(.25) // vcpu - 1 vCPU is equivalent to 1,024 CPU
                .memory(Size.mebibytes(model().batchRuntimeProps().memorySizeMB()))
                .build();

        jobDefinition = EcsJobDefinition.Builder.create(this, "JobDef")
                .jobDefinitionName(regionalName.lowerHyphen()) // physical name
                .container(container)
                .retryAttempts(model().batchRuntimeProps().retryAttempts())
                .timeout(Duration.minutes(model().batchRuntimeProps().timeoutMin()))
                .build();

        IManagedComputeEnvironment computeEnvironment = resolveComputeEnvironment(model().computeEnvironmentRef());

        jobQueue = JobQueue.Builder.create(this, "JobQueue")
                .jobQueueName(regionalName.lowerHyphen())
                .build();

        jobQueue.addComputeEnvironment(computeEnvironment, 1);

        BatchJob batchJob = BatchJob.Builder.create(
                        jobQueue.getJobQueueArn(),
                        jobQueue,
                        jobDefinition.getJobDefinitionArn(),
                        jobDefinition
                )
                .jobName(regionalName.camelCase())
                .build();

        createScheduledRule(LOG, this, model.name(), model.schedule(), batchJob, model.enabled());
    }

    @NotNull
    protected IManagedComputeEnvironment resolveComputeEnvironment(String computeEnvironmentRef) {

        LOG.info("resolving computeEnvironment ref: {}", computeEnvironmentRef);

        return importArnRef(computeEnvironmentRef, arn -> {
            LOG.info("using computeEnvironment arn: {}", arn);
            return FargateComputeEnvironment.fromFargateComputeEnvironmentArn(
                    this,
                    "ComputeEnvironment",
                    arn);
        });
    }
}
