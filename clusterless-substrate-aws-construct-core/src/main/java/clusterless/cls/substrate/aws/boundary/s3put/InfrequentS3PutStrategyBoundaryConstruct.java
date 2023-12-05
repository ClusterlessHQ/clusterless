/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.boundary.s3put;

import clusterless.aws.lambda.boundary.s3put.S3PutBoundaryProps;
import clusterless.cls.model.deploy.SinkDataset;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.substrate.aws.construct.ExtensibleConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.props.Lookup;
import clusterless.cls.substrate.aws.resource.s3.S3BucketResourceConstruct;
import clusterless.cls.substrate.aws.resources.*;
import clusterless.cls.substrate.uri.ManifestURI;
import clusterless.cls.util.Env;
import clusterless.cls.util.URIs;
import clusterless.commons.collection.OrderedSafeMaps;
import clusterless.commons.collection.SafeList;
import clusterless.commons.naming.Label;
import clusterless.commons.substrate.aws.cdk.construct.LambdaLogGroupConstruct;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedStack;
import clusterless.commons.temporal.IntervalUnits;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.events.EventPattern;
import software.amazon.awscdk.services.events.IEventBus;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.lambda.Function;
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
public class InfrequentS3PutStrategyBoundaryConstruct extends ExtensibleConstruct<S3PutListenerBoundary> {
    private static final Logger LOG = LogManager.getLogger(InfrequentS3PutStrategyBoundaryConstruct.class);

    public InfrequentS3PutStrategyBoundaryConstruct(@NotNull ManagedComponentContext context, @NotNull S3PutListenerBoundary model) {
        super(context, model, Label.of("Infrequent").with(model.name()));

        // confirm unit exits
        TemporalUnit temporalUnit = IntervalUnits.find(model().lotUnit());
        IntervalUnits.verifyHasFormatter(temporalUnit);

        URI listenURI = URIs.normalizeURI(model().dataset().pathURI());

        String listenBucketName = listenURI.getHost();
        String listenPathPrefix = URIs.asKeyPath(listenURI);
        String manifestBucketNameRef = BootstrapStores.manifestStoreNameRef(this);
        String eventBusRef = Events.arcEventBusNameRef(this);
        String listenerRuleName = Rules.ruleName(this, model.name()).lowerHyphen();

        IBucket listenBucket = Bucket.fromBucketName(this, "ListenBucket", listenBucketName);
        IBucket manifestBucket = Bucket.fromBucketName(this, "ManifestBucket", manifestBucketNameRef);
        IEventBus arcEventBus = EventBus.fromEventBusName(this, "EventBus", eventBusRef);

        // declare lambda to convert put event into arc event
        ManifestURI manifestComplete = StateURIs.manifestPath(this, ManifestState.complete, model().dataset());
        ManifestURI manifestPartial = StateURIs.manifestPath(this, ManifestState.partial, model().dataset());

        S3PutBoundaryProps transformProps = S3PutBoundaryProps.builder()
                .withEventBusName(eventBusRef)
                .withDataset(SinkDataset.Builder.builder()
                        .withName(model().dataset().name())
                        .withVersion(model.dataset().version())
                        .withPublish(model.dataset().publish())
                        .withPathURI(listenURI)
                        .build())
                .withManifestCompletePath(manifestComplete)
                .withManifestPartialPath(manifestPartial)
                .withLotUnit(model.lotUnit())
                .withLotSource(model().infrequent().lotSource())
                .withKeyRegex(model().infrequent().keyRegex())
                .withFilter(model().filter())
                .build();

        Map<String, String> environment = Env.toEnv(transformProps);

        String functionName = Functions.functionName(this, model().name(), "Int");
        Label functionLabel = Label.of(model().name()).with("Int");
        Function transformEventFunction = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionName)
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-transform-.*\\.zip$"))) // get packaged code
                .handler("clusterless.aws.lambda.boundary.s3put.PutEventBoundaryHandler") // get handler class name
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

        arcEventBus.grantPutEventsTo(transformEventFunction);
        manifestBucket.grantReadWrite(transformEventFunction);
        listenBucket.grantRead(transformEventFunction);

        // if the bucket was not declared with eventBridgeNotification enabled, it must be set here
        // as of 2.64.0 a lambda is installed -> https://github.com/aws/aws-cdk/issues/24086
        // note that multiple boundaries can share the same bucket, if they all enable eventbridge, there can be
        // a type of race condition in cloudformation.
        // it's best this is enabled once during a deploy
        if (model().infrequent.enableEventBridge()) {
            // todo: inject warning about
            //  Custom::S3BucketNotifications
            //  Received response status [FAILED] from custom resource. Message returned: Error: An error occurred (OperationAborted) when calling the PutBucketNotificationConfiguration operation: A conflicting conditional operation is currently in progress against this resource. Please try again.. See the details in CloudWatch Log
            //  attempts to prevent PutBucketNotificationConfiguration errors if the bucket is yet available
            //  if the bucket doesn't exist in this stack, it should already be created in another stack
            List<S3BucketResourceConstruct> list = ScopedStack.scopedOf(this)
                    .findHaving(S3BucketResourceConstruct.class)
                    .filter(b -> b.model().bucketName().equals(listenBucketName))
                    .toList();

            if (list.isEmpty()) {
                LOG.info("enabling event bridge notification on external bucket: {}", listenBucketName);
                listenBucket.enableEventBridgeNotification(); // places put events into default event bus
            } else {
                list.forEach(b -> {
                    LOG.info("enabling event bridge notification on local bucket: {}", listenBucketName);
                    b.bucket().enableEventBridgeNotification(); // places put events into default event bus
                });
            }
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

        LOG.info("creating rule pattern for: {}:{}", pattern.getSource(), pattern.getDetailType());

        Rule.Builder.create(this, "ListenerEvent")
                .ruleName(listenerRuleName)
                .enabled(true)
                .eventPattern(pattern)
                .targets(List.of(lambdaFunction))
                .build();
    }
}
