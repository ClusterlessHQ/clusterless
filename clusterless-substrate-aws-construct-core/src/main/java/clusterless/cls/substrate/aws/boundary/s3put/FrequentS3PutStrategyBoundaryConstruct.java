/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.boundary.s3put;

import clusterless.aws.lambda.boundary.frequents3put.FrequentS3PutBoundaryProps;
import clusterless.cls.model.deploy.SinkDataset;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.substrate.aws.construct.ExtensibleConstruct;
import clusterless.cls.substrate.aws.construct.IsScheduled;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.props.Lookup;
import clusterless.cls.substrate.aws.resource.s3.S3BucketResourceConstruct;
import clusterless.cls.substrate.aws.resources.*;
import clusterless.cls.substrate.uri.ManifestURI;
import clusterless.cls.util.Env;
import clusterless.cls.util.URIs;
import clusterless.commons.naming.Label;
import clusterless.commons.substrate.aws.cdk.construct.LambdaLogGroupConstruct;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.events.IEventBus;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.amazon.awscdk.services.s3.NotificationKeyFilter;
import software.amazon.awscdk.services.s3.notifications.SqsDestination;
import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.sqs.QueueEncryption;

import java.net.URI;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 */
public class FrequentS3PutStrategyBoundaryConstruct extends ExtensibleConstruct<S3PutListenerBoundary> implements IsScheduled {
    private static final Logger LOG = LoggerFactory.getLogger(FrequentS3PutStrategyBoundaryConstruct.class);

    public FrequentS3PutStrategyBoundaryConstruct(@NotNull ManagedComponentContext context, @NotNull S3PutListenerBoundary model) {
        super(context, model, Label.of("Frequent").with(model.name()));

        Frequent frequent = model().frequent();

        // confirm unit exits
        TemporalUnit temporalUnit = verifiedTemporalUnit(model().lotUnit());

        URI listenURI = URIs.normalizeURI(model().dataset().pathURI());

        String listenBucketName = listenURI.getHost();
        String listenPathPrefix = URIs.asKeyPath(listenURI); // slash at end
        String manifestBucketNameRef = BootstrapStores.manifestStoreNameRef(this);
        String eventBusRef = Events.arcEventBusNameRef(this);

        IBucket listenBucket = Bucket.fromBucketName(this, "ListenBucket", listenBucketName);
        IBucket manifestBucket = Bucket.fromBucketName(this, "ManifestBucket", manifestBucketNameRef);
        IEventBus arcEventBus = EventBus.fromEventBusName(this, "EventBus", eventBusRef);

        String queueName = Queues.queueName(this, model().name()).lowerHyphen();

        Queue s3EventQueue = Queue.Builder.create(this, "S3EventQueue")
                // with KMS_MANAGED enabled, there is a 'validation' error on deploy
                // this is a permission problem
                .encryption(QueueEncryption.UNENCRYPTED)
                .enforceSsl(false)
                .removalPolicy(RemovalPolicy.DESTROY)
                .retentionPeriod(Duration.days(4)) // 4 days is default
                .queueName(queueName)
                .build();

        // attempts to prevent PutBucketNotificationConfiguration errors if the bucket is yet available
        // if the bucket doesn't exist in this stack, it should already be created in another stack
        ScopedStack.scopedOf(this)
                .findHaving(S3BucketResourceConstruct.class)
                .filter(b -> b.model().bucketName().equals(listenBucketName))
                .forEach(b -> {
                    LOG.info("adding dependency on bucket: {} for queue: {}", listenBucketName, queueName);
                    s3EventQueue.getNode().addDependency(b);
                });

        // declare lambda to convert put event into arc event
        ManifestURI manifestComplete = StateURIs.manifestPath(this, ManifestState.complete, model().dataset());
        ManifestURI manifestPartial = StateURIs.manifestPath(this, ManifestState.partial, model().dataset());

        FrequentS3PutBoundaryProps transformProps = FrequentS3PutBoundaryProps.builder()
                .withEventBusName(eventBusRef)
                .withSqsQueueName(queueName)
                .withSqsWaitTimeSeconds(frequent.queueFetchWaitSec())
                .withDataset(SinkDataset.Builder.builder()
                        .withName(model().dataset().name())
                        .withVersion(model.dataset().version())
                        .withPublish(model.dataset().publish())
                        .withPathURI(listenURI)
                        .build())
                .withManifestCompletePath(manifestComplete)
                .withManifestPartialPath(manifestPartial)
                .withLotUnit(model.lotUnit())
                .withFilter(model().filter())
                .build();

        Map<String, String> environment = Env.toEnv(transformProps);

        String functionName = Functions.functionName(this, model().name(), "Int");
        Label functionLabel = Label.of(model().name()).with("Int");
        Function transformEventFunction = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionName)
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-transform-.*\\.zip$"))) // get packaged code
                .handler("clusterless.aws.lambda.boundary.frequents3put.FrequentPutEventBoundaryHandler") // get handler class name
                .environment(environment)
                .runtime(Functions.defaultJVM())
                .memorySize(model().runtimeProps().memorySizeMB())
                .timeout(Duration.minutes(model().runtimeProps().timeoutMin()))
                .architecture(Lookup.architecture(model().runtimeProps().architecture()))
                .build();

        LambdaFunction lambdaFunction = LambdaFunction.Builder.create(transformEventFunction)
                .retryAttempts(model().runtimeProps().retryAttempts())
                .build();

        new LambdaLogGroupConstruct(this, functionLabel, transformEventFunction);

        s3EventQueue.grantConsumeMessages(transformEventFunction);
        arcEventBus.grantPutEventsTo(transformEventFunction);
        manifestBucket.grantReadWrite(transformEventFunction);
        listenBucket.grantRead(transformEventFunction);

        // performs a PutBucketNotificationConfiguration operation to the S3 API
        // this may fail if the bucket creation is not yet completed
        listenBucket.addObjectCreatedNotification(
                new SqsDestination(s3EventQueue),
                NotificationKeyFilter.builder()
                        .prefix(listenPathPrefix)
                        .build()
        );

        Schedule schedule = scheduleFromTemporalUnit(temporalUnit).orElseThrow(() -> new UnsupportedOperationException("unsupported temporal unit: " + temporalUnit));
        createScheduledRule(LOG, this, model().name(), lambdaFunction, schedule, frequent.enabled());
    }
}
