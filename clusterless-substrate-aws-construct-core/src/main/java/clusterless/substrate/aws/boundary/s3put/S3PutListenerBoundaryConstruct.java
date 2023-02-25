/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.lambda.transform.PutEventTransformHandler;
import clusterless.lambda.transform.TransformProps;
import clusterless.managed.component.BoundaryComponent;
import clusterless.substrate.aws.construct.IngressBoundaryConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.construct.ModelConstruct;
import clusterless.substrate.aws.resources.Assets;
import clusterless.substrate.aws.resources.Buckets;
import clusterless.substrate.aws.resources.Events;
import clusterless.substrate.aws.resources.Rules;
import clusterless.temporal.IntervalUnits;
import clusterless.util.*;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.events.EventPattern;
import software.amazon.awscdk.services.events.IEventBus;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;

import java.net.URI;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 */
public class S3PutListenerBoundaryConstruct extends IngressBoundaryConstruct<S3PutListenerBoundary> {

    RetentionDays retentionDays = RetentionDays.ONE_DAY;
    RemovalPolicy removalPolicy = RemovalPolicy.DESTROY;

    private final Rule rule;

    public S3PutListenerBoundaryConstruct(@NotNull ManagedComponentContext context, @NotNull S3PutListenerBoundary model) {
        super(context, model);

        // confirm unit exits
        TemporalUnit temporalUnit = IntervalUnits.find(model().lotUnit());
        IntervalUnits.verifyHasFormatter(temporalUnit);

        URI listenURI = URIs.normalizeURI(model().listenURI());

        String listenBucketName = listenURI.getHost();
        String listenPathPrefix = URIs.asKeyPath(listenURI);
        String manifestBucketName = Buckets.bootstrapManifestBucketNameRef(this);
        String eventBusName = Events.arcEventBusNameRef(this);
        String listenerRuleName = Rules.ruleName(this, model.name());

        IBucket listenBucket = Bucket.fromBucketName(this, "ListenBucket", listenBucketName);
        IBucket manifestBucket = Bucket.fromBucketName(this, "ManifestBucket", manifestBucketName);
        IEventBus arcEventBus = EventBus.fromEventBusName(this, "EventBus", eventBusName);

        // declare lambda to convert put event into arc event
        URI manifestPrefix = Buckets.bootstrapManifestURI(this, model().dataset().name(), model().dataset().version());

        TransformProps transformProps = TransformProps.Builder.builder()
                .withEventBusName(eventBusName)
                .withDatasetPrefix(listenURI)
                .withDatasetName(model().dataset().name())
                .withDatasetVersion(model().dataset().version())
                .withManifestPrefix(manifestPrefix)
                .withLotUnit(model.lotUnit())
                .withLotSource(model().lotSource())
                .withKeyRegex(model().keyRegex())
                .build();

        Map<String, String> environment = Env.toEnv(transformProps);

        Label functionLabel = Label.of(model().name()).with("TransformPutEvent");
        Function transformEventFunction = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionLabel.lowerHyphen())
                .code(Assets.find(Pattern.compile("^.*-aws-service-transform-.*\\.zip$"))) // get packaged code
                .handler(PutEventTransformHandler.class.getName()) // get handler class name
                .environment(environment)
                .runtime(Runtime.JAVA_11)
                .memorySize(model().runtimeProps().memorySizeMB())
                .timeout(Duration.minutes(model().runtimeProps().timeoutMin()))
                .build();

        LogGroup.Builder.create(this, Label.of("LogGroup").with(functionLabel).camelCase())
                .logGroupName("/aws/lambda/" + transformEventFunction.getFunctionName())
                .removalPolicy(removalPolicy)
                .retention(retentionDays)
                .build();

        // todo: allow access to cloudwatch

        arcEventBus.grantPutEventsTo(transformEventFunction);
        manifestBucket.grantReadWrite(transformEventFunction);
        listenBucket.grantRead(transformEventFunction);
        // as of 2.64.0 a lambda is installed -> https://github.com/aws/aws-cdk/issues/24086
        // note that multiple boundaries can share the same bucket, if they all enable eventbridge, there can be
        // a type of race condition in cloudformation.
        // it's best this is enabled once during a deploy
        if (model().enableEventBridge()) {
            // todo: inject warning about
            //  Custom::S3BucketNotifications
            // Received response status [FAILED] from custom resource. Message returned: Error: An error occurred (OperationAborted) when calling the PutBucketNotificationConfiguration operation: A conflicting conditional operation is currently in progress against this resource. Please try again.. See the details in CloudWatch Log
            listenBucket.enableEventBridgeNotification(); // places put events into default event bus
        }

        EventPattern pattern = EventPattern.builder()
                .source(List.of("aws.s3"))
                .detailType(List.of("Object Created"))
                .detail(OrderedSafeMaps.of(
                        "bucket.name", SafeList.of(listenBucketName),
                        "object.key", SafeList.of(
                                OrderedSafeMaps.of(
                                        "prefix", listenPathPrefix
                                )
                        )
                ))
                .build();

        LambdaFunction lambdaFunction = LambdaFunction.Builder.create(transformEventFunction)
                .retryAttempts(model().runtimeProps().retryAttempts())
                .build();

        rule = Rule.Builder.create(this, "ListenerEvent")
                .ruleName(listenerRuleName)
                .enabled(true)
                .eventPattern(pattern)
                .targets(List.of(lambdaFunction))
                .build();
    }
}