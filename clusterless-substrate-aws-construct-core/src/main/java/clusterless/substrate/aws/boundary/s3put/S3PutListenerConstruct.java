/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.managed.component.Component;
import clusterless.substrate.aws.managed.Lambdas;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.model.ModelConstruct;
import clusterless.util.Label;
import clusterless.util.OrderedSafeMaps;
import clusterless.util.SafeList;
import clusterless.util.URIs;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
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

import java.util.List;
import java.util.Map;

/**
 *
 */
public class S3PutListenerConstruct extends ModelConstruct<S3PutListenerBoundary> implements Component {

    private final Rule rule;

    public S3PutListenerConstruct(@NotNull ManagedComponentContext context, @NotNull S3PutListenerBoundary model) {
        super(context, model, model.listenBucketURI().getHost());

        String listenBucketName = model().listenBucketURI().getHost();
        String listenBucketPrefix = URIs.asPrefix(model().listenBucketURI());
        String manifestBucketName = model().manifestBucketURI().getHost();
        String eventBusName = model().eventBusName();
        String listenerRuleName = model().listenerRuleName();

        IBucket listenBucket = Bucket.fromBucketName(this, "ListenBucket", listenBucketName);
        IBucket manifestBucket = Bucket.fromBucketName(this, "ManifestBucket", manifestBucketName);
        IEventBus eventBus = EventBus.fromEventBusName(this, "EventBus", eventBusName);

        // declare lambda to convert put event into arc event
        Map<String, String> environment = OrderedSafeMaps.of(
                Label.of("ListenURI").upperUnderscore(), model().listenBucketURI().toString(),
                Label.of("ManifestURI").upperUnderscore(), model().manifestBucketURI().toString(),
                Label.of("EventBusName").upperUnderscore(), eventBusName
        );

        Function transformEventFunction = Function.Builder.create(this, "TransformPutEvent")
                .functionName(Label.of("TransformPutEvent").lowerHyphen())
                .code(Lambdas.find())
                .handler("")
                .environment(environment)
                .runtime(Runtime.JAVA_11)
                .memorySize(model().memorySizeMB())
                .timeout(Duration.minutes(model().timeoutMin()))
                .build();

        // allow access to cloudwatch


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
                                        "prefix", listenBucketPrefix
                                )
                        )
                ))
                .build();

        LambdaFunction lambdaFunction = LambdaFunction.Builder.create(transformEventFunction)
                .retryAttempts(model().retryAttempts())
                .build();

        rule = Rule.Builder.create(this, "ListenerEvent")
                .ruleName(listenerRuleName)
                .enabled(true)
                .eventPattern(pattern)
                .targets(List.of(lambdaFunction))
                .build();

        new CfnOutput(this, id("ListenBucketURI"), new CfnOutputProps.Builder()
                .exportName("s3:%s:name".formatted(model().listenBucketURI().getHost()))
                .value(model.listenBucketURI().toString())
                .description("list bucket uri")
                .build());

    }
}
