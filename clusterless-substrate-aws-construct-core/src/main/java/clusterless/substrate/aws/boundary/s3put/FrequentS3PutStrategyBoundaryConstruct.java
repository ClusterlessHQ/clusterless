/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.lambda.transform.frequents3put.FrequentS3PutTransformProps;
import clusterless.model.deploy.Dataset;
import clusterless.model.manifest.ManifestState;
import clusterless.naming.Label;
import clusterless.substrate.aws.construct.LambdaLogGroupConstruct;
import clusterless.substrate.aws.construct.ModelConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.props.Lookup;
import clusterless.substrate.aws.resources.*;
import clusterless.substrate.uri.ManifestURI;
import clusterless.temporal.IntervalUnits;
import clusterless.util.Env;
import clusterless.util.URIs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.events.*;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 */
public class FrequentS3PutStrategyBoundaryConstruct extends ModelConstruct<S3PutListenerBoundary> {
    private static final Logger LOG = LogManager.getLogger(FrequentS3PutStrategyBoundaryConstruct.class);

    public FrequentS3PutStrategyBoundaryConstruct(@NotNull ManagedComponentContext context, @NotNull S3PutListenerBoundary model) {
        super(context, model, Label.of("Frequent").with(model.name()).camelCase());

        // confirm unit exits
        TemporalUnit temporalUnit = IntervalUnits.find(model().lotUnit());
        IntervalUnits.verifyHasFormatter(temporalUnit);

        URI listenURI = URIs.normalizeURI(model().dataset().pathURI());

        String listenBucketName = listenURI.getHost();
        String listenPathPrefix = URIs.asKeyPath(listenURI); // slash at end
        String manifestBucketNameRef = BootstrapStores.manifestStoreNameRef(this);
        String eventBusRef = Events.arcEventBusNameRef(this);
        String listenerRuleName = Rules.ruleName(this, model.name()).lowerHyphen();

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

        // declare lambda to convert put event into arc event
        ManifestURI manifestComplete = StateURIs.manifestPath(this, ManifestState.complete, model().dataset());
        ManifestURI manifestPartial = StateURIs.manifestPath(this, ManifestState.partial, model().dataset());

        FrequentS3PutTransformProps transformProps = FrequentS3PutTransformProps.builder()
                .withEventBusName(eventBusRef)
                .withSqsQueueName(queueName)
                .withSqsWaitTimeSeconds(model().frequent().queueFetchWaitSec())
                .withDataset(Dataset.builder()
                        .withName(model().dataset().name())
                        .withVersion(model.dataset().version())
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
                .handler("clusterless.lambda.transform.frequents3put.FrequentPutEventTransformHandler") // get handler class name
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

        listenBucket.addObjectCreatedNotification(
                new SqsDestination(s3EventQueue),
                NotificationKeyFilter.builder()
                        .prefix(listenPathPrefix)
                        .build()
        );

        // https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-cron-expressions.html
        if (temporalUnit.getDuration().toMinutes() > 60) {
            throw new UnsupportedOperationException("temporal unit greater than 60 minutes: " + model().lotUnit());
        }
        String cronMinute = String.format("0/%d", temporalUnit.getDuration().toMinutes());
        Schedule schedule = Schedule.cron(CronOptions.builder()
                .minute(cronMinute)
                .build());

        LOG.info("creating rule schedule with cron minute: {}", cronMinute);

        Rule.Builder.create(this, "ListenerEvent")
                .ruleName(listenerRuleName)
                .enabled(true)
                .schedule(schedule)
                .targets(List.of(lambdaFunction))
                .build();
    }
}
