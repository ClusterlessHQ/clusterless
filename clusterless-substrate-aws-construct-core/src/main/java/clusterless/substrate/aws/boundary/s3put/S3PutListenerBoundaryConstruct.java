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
import clusterless.model.deploy.Dataset;
import clusterless.model.manifest.ManifestState;
import clusterless.naming.Label;
import clusterless.substrate.aws.construct.IngressBoundaryConstruct;
import clusterless.substrate.aws.construct.LambdaLogGroupConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.props.Lookup;
import clusterless.substrate.aws.resources.*;
import clusterless.substrate.uri.ManifestURI;
import clusterless.temporal.IntervalUnits;
import clusterless.util.Env;
import clusterless.util.OrderedSafeMaps;
import clusterless.util.SafeList;
import clusterless.util.URIs;
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

        URI listenURI = URIs.normalizeURI(model().dataset().pathURI());

        String listenBucketName = listenURI.getHost();
        String listenPathPrefix = URIs.asKey(listenURI);
        String manifestBucketNameRef = BootstrapStores.manifestStoreNameRef(this);
        String eventBusRef = Events.arcEventBusNameRef(this);
        String listenerRuleName = Rules.ruleName(this, model.name()).lowerHyphen();

        IBucket listenBucket = Bucket.fromBucketName(this, "ListenBucket", listenBucketName);
        IBucket manifestBucket = Bucket.fromBucketName(this, "ManifestBucket", manifestBucketNameRef);
        IEventBus arcEventBus = EventBus.fromEventBusName(this, "EventBus", eventBusRef);

        // declare lambda to convert put event into arc event
        ManifestURI manifestComplete = StateURIs.manifestPath(this, ManifestState.complete, model().dataset());
        ManifestURI manifestPartial = StateURIs.manifestPath(this, ManifestState.partial, model().dataset());

        TransformProps transformProps = TransformProps.builder()
                .withEventBusName(eventBusRef)
                .withDataset(Dataset.Builder.builder()
                        .withName(model().dataset().name())
                        .withVersion(model.dataset().version())
                        .withPathURI(listenURI)
                        .build())
                .withManifestCompletePath(manifestComplete)
                .withManifestPartialPath(manifestPartial)
                .withLotUnit(model.lotUnit())
                .withLotSource(model().lotSource())
                .withKeyRegex(model().keyRegex())
                .build();

        Map<String, String> environment = Env.toEnv(transformProps);

        String functionName = Functions.functionName(this, model().name(), "Int");
        Label functionLabel = Label.of(model().name()).with("Int");
        Function transformEventFunction = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionName)
                .code(Assets.find(Pattern.compile("^.*-aws-service-transform-.*\\.zip$"))) // get packaged code
                .handler(PutEventTransformHandler.class.getName()) // get handler class name
                .environment(environment)
                .runtime(Runtime.JAVA_11)
                .memorySize(model().runtimeProps().memorySizeMB())
                .timeout(Duration.minutes(model().runtimeProps().timeoutMin()))
                .architecture(Lookup.architecture(model().runtimeProps().architecture()))
                .build();

        new LambdaLogGroupConstruct(this, functionLabel, transformEventFunction);

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
