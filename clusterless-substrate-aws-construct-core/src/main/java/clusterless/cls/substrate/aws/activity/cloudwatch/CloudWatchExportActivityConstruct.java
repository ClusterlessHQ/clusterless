/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.activity.cloudwatch;

import clusterless.aws.lambda.activity.cloudwatch.CloudWatchExportActivityProps;
import clusterless.cls.substrate.aws.construct.ActivityConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.props.Lookup;
import clusterless.cls.substrate.aws.resources.Assets;
import clusterless.cls.substrate.aws.resources.Functions;
import clusterless.cls.substrate.aws.resources.Rules;
import clusterless.cls.util.Env;
import clusterless.commons.collection.OrderedSafeMaps;
import clusterless.commons.naming.Label;
import clusterless.commons.substrate.aws.cdk.construct.LambdaLogGroupConstruct;
import clusterless.commons.temporal.IntervalUnits;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.events.CronOptions;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.Schedule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.iam.*;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.logs.ILogGroup;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;

import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 */
public class CloudWatchExportActivityConstruct extends ActivityConstruct<CloudWatchExportActivity> {
    private static final Logger LOG = LogManager.getLogger(CloudWatchExportActivityConstruct.class);

    public CloudWatchExportActivityConstruct(@NotNull ManagedComponentContext context, @NotNull CloudWatchExportActivity model) {
        super(context, model, model.name());

        // confirm unit exits
        // currently only support IntervalUnits
        // todo: add support for rate and cron
        TemporalUnit temporalUnit = IntervalUnits.find(model().interval());
        IntervalUnits.verifyHasFormatter(temporalUnit);

        CloudWatchExportActivityProps activityProps = CloudWatchExportActivityProps.builder()
                .withDestinationURI(model.destinationURI())
                .withLogGroupName(model.logGroupName())
                .withLogStreamPrefix(model.logStreamPrefix())
                .withInterval(model.interval())
                .build();

        Map<String, String> environment = Env.toEnv(activityProps);

        String functionName = Functions.functionName(this, model().name(), "Int");
        Label functionLabel = Label.of(model().name()).with("Int");
        Function function = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionName)
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-transform-.*\\.zip$"))) // get packaged code
                .handler("clusterless.aws.lambda.activity.cloudwatch.CloudWatchExportActivityHandler") // get handler class name
                .environment(environment)
                .runtime(Functions.defaultJVM())
                .memorySize(model().runtimeProps().memorySizeMB())
                .timeout(Duration.minutes(model().runtimeProps().timeoutMin()))
                .architecture(Lookup.architecture(model().runtimeProps().architecture()))
                .build();

        LambdaFunction lambdaFunction = LambdaFunction.Builder.create(function)
                .retryAttempts(model().runtimeProps().retryAttempts())
                .build();

        new LambdaLogGroupConstruct(this, functionLabel, function);

        ILogGroup declaredLogGroup = LogGroup.fromLogGroupName(this, "DeclaredLogGroup", model.logGroupName());

        declaredLogGroup.grant(function, "logs:CreateExportTask");

        IBucket destinationBucket = Bucket.fromBucketName(this, "DestinationBucket", model.destinationURI().getHost());

        String region = context.deployable().placement().region();
        ServicePrincipal principal = new ServicePrincipal(ServicePrincipal.servicePrincipalName("logs.amazonaws.com"));

        Grant grant = destinationBucket
                .grantWrite(principal);

        grant.assertSuccess();

        // https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/S3ExportTasks.html
        String account = context.deployable().placement().account();
        AddToResourcePolicyResult policyResult = destinationBucket.addToResourcePolicy(PolicyStatement.Builder.create()
                .principals(List.of(principal))
                .actions(List.of("s3:GetBucketAcl"))
                .resources(List.of(destinationBucket.getBucketArn()))
                .conditions(OrderedSafeMaps.of(
                                "StringEquals", Map.of("aws:SourceAccount", List.of(account)),
                                "ArnLike", Map.of("aws:SourceArn", List.of("arn:aws:logs:%s:%s:%s:*".formatted(region, account, model.logGroupName())))
                        )
                )
                .effect(Effect.ALLOW)
                .build());

        if (policyResult.getStatementAdded()) {
            throw new IllegalStateException("failed to add policy statement to bucket: " + destinationBucket.getBucketName());
        }

        // https://docs.aws.amazon.com/eventbridge/latest/userguide/eb-cron-expressions.html
        if (temporalUnit.getDuration().toMinutes() > 60) {
            throw new UnsupportedOperationException("interval greater than 60 minutes: " + model().interval());
        }
        String cronMinute = String.format("0/%d", temporalUnit.getDuration().toMinutes());
        Schedule schedule = Schedule.cron(CronOptions.builder()
                .minute(cronMinute)
                .build());

        LOG.info("creating rule interval using: {}", schedule);

        String listenerRuleName = Rules.ruleName(this, model.name()).lowerHyphen();

        Rule.Builder.create(this, "ListenerEvent")
                .ruleName(listenerRuleName)
                .enabled(true)
                .schedule(schedule)
                .targets(List.of(lambdaFunction))
                .build();
    }
}
