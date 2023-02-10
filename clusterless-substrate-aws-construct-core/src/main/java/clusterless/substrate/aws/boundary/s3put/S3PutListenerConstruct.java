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
import clusterless.managed.component.Component;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.model.ModelConstruct;
import clusterless.substrate.aws.resources.Buckets;
import clusterless.substrate.aws.resources.Events;
import clusterless.substrate.aws.resources.Lambdas;
import clusterless.substrate.aws.resources.Rules;
import clusterless.temporal.IntervalUnits;
import clusterless.util.*;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.events.EventPattern;
import software.amazon.awscdk.services.events.IEventBus;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;

import java.net.URI;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class S3PutListenerConstruct extends ModelConstruct<S3PutListenerBoundary> implements Component {

    private final Rule rule;

    public S3PutListenerConstruct(@NotNull ManagedComponentContext context, @NotNull S3PutListenerBoundary model) {
        super(context, model, model.listenURI().getHost());

        // confirm unit exits
        TemporalUnit temporalUnit = IntervalUnits.find(model().lotUnit());
        IntervalUnits.verifyHasFormatter(temporalUnit);

        URI listenURI = URIs.normalizeURI(model().listenURI());

        String listenBucketName = listenURI.getHost();
        String listenPathPrefix = URIs.asPathPrefix(listenURI);
        String manifestBucketName = Buckets.bootstrapManifestBucketName(this);
        String eventBusName = Events.arcEventBusName(this);
        String listenerRuleName = Rules.ruleName(this, model.boundaryName());

        IBucket listenBucket = Bucket.fromBucketName(this, "ListenBucket", listenBucketName);
        IBucket manifestBucket = Bucket.fromBucketName(this, "ManifestBucket", manifestBucketName);
        IEventBus eventBus = EventBus.fromEventBusName(this, "EventBus", eventBusName);

        // declare lambda to convert put event into arc event
        TransformProps transformProps = TransformProps.Builder.builder()
                .withManifestPrefix(Buckets.bootstrapManifestURI(this, model().boundaryName()))
                .withLotUnit(model.lotUnit())
                .withLotSource(model().lotSource())
                .withKeyRegex(model().keyRegex())
                .build();

        Map<String, String> environment = Env.toEnv(transformProps);

        Function transformEventFunction = Function.Builder.create(this, "TransformPutEvent")
                .functionName(Label.of("TransformPutEvent").lowerHyphen())
                .code(Lambdas.find()) // get packaged code
                .handler(PutEventTransformHandler.class.getName()) // get handler class name
                .environment(environment)
                .runtime(Runtime.JAVA_11)
                .memorySize(model().runtimeProps().memorySizeMB())
                .timeout(Duration.minutes(model().runtimeProps().timeoutMin()))
                .build();

        // todo: allow access to cloudwatch


        eventBus.grantPutEventsTo(transformEventFunction);
        manifestBucket.grantReadWrite(transformEventFunction);
        listenBucket.grantRead(transformEventFunction);

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

//        new CfnOutput(this, id("ListenBucketURI"), new CfnOutputProps.Builder()
//                .exportName("s3:%s:name".formatted(Label.of(model().boundaryName()).lowerHyphen()))
//                .value(listenPathPrefix)
//                .description("listener bucket uri")
//                .build());

    }
}
