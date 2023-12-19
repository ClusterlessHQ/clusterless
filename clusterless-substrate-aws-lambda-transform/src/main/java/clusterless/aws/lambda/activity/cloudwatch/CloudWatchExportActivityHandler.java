/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.activity.cloudwatch;

import clusterless.aws.lambda.EventHandler;
import clusterless.aws.lambda.transform.json.event.AWSEvent;
import clusterless.cls.substrate.aws.sdk.ClientRetry;
import clusterless.cls.substrate.aws.sdk.CloudWatchLogs;
import clusterless.cls.util.Env;
import clusterless.commons.temporal.IntervalBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.LimitExceededException;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;

public class CloudWatchExportActivityHandler extends EventHandler<AWSEvent, CloudWatchExportActivityObserver> {
    private static final Logger LOG = LogManager.getLogger(CloudWatchExportActivityHandler.class);
    protected final CloudWatchExportActivityProps activityProps = Env.fromEnv(
            CloudWatchExportActivityProps.class,
            () -> CloudWatchExportActivityProps.builder()
                    .build()
    );

    protected final CloudWatchLogs cloudWatchLogs = new CloudWatchLogs();

    protected final ClientRetry<CloudWatchLogsClient> retryClient = new ClientRetry<>(
            "cloudwatchlogs",
            // attempt to match the lambda timeout period, minus a small fudge factor
            Duration.ofMinutes(activityProps.timeoutMin()).minusSeconds(5),
            ClientRetry.exponentialBackoff(Duration.ofSeconds(3), 2.0, Duration.ofMinutes(3)),
            r -> r.exception() instanceof LimitExceededException
    );

    protected final IntervalBuilder intervalBuilder = new IntervalBuilder(activityProps.interval);

    public CloudWatchExportActivityHandler() {
        super(AWSEvent.class);
    }

    protected CloudWatchExportActivityObserver observer() {
        return new CloudWatchExportActivityObserver() {
            @Override
            public void applyEvent(OffsetDateTime time) {
                LOG.info("received, time: {}", time);
            }

            @Override
            public void applyInterval(String interval) {
                LOG.info("interval: {}", interval);
            }

            @Override
            public void applyRange(Instant from, Instant to) {
                LOG.info("range: {}, {}", from, to);
            }
        };
    }

    @Override
    public void handleEvent(AWSEvent event, Context context, CloudWatchExportActivityObserver eventObserver) {
        OffsetDateTime scheduledTime = event.getTime();

        eventObserver.applyEvent(scheduledTime);

        // skip any message that happened in the current lot interval
        // a small attempt to maintain flow control
        Instant arrivalTime = intervalBuilder.truncate(scheduledTime).toInstant();

        // use the lot previous to the current event time
        Instant startTimeInclusive = intervalBuilder.previous(arrivalTime);
        Instant endTimeInclusive = arrivalTime.minusMillis(1);

        eventObserver.applyRange(startTimeInclusive, endTimeInclusive);

        String interval = intervalBuilder.format(startTimeInclusive);

        eventObserver.applyInterval(interval);

        Stopwatch getStopwatch = Stopwatch.createUnstarted();

        String taskName = "export-" + interval;
        String logGroupName = activityProps.logGroupName();
        String logStreamPrefix = activityProps.logStreamPrefix();
        URI destination = activityProps.pathURI();

        if (!eventObserver.enableExport()) {
            LOG.info("export disabled");
            return;
        }

        // there is a service quota here: https://docs.aws.amazon.com/AmazonCloudWatch/latest/logs/cloudwatch_limits_cwl.html
        // Export task: One active (running or pending) export task at a time, per account. This quota can't be changed.
        // Caused by: software.amazon.awssdk.services.cloudwatchlogs.model.LimitExceededException: Resource limit exceeded. (Service: CloudWatchLogs, Status Code: 400, Request ID: 79069256-8403-4435-a85c-17013c7d7c4c)
        getStopwatch.start();
        CloudWatchLogs.Response response = retryClient.invoke(() -> cloudWatchLogs.createExportLogGroupTask(taskName, logGroupName, logStreamPrefix, destination, startTimeInclusive, endTimeInclusive));
        getStopwatch.stop();

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to create export task: %s, %s, %s", logGroupName, destination, r.errorMessage())
        );

        Duration getElapsed = getStopwatch.elapsed();
        LOG.info("export task: {}, duration: {}", cloudWatchLogs.exportTask(response), getElapsed);
    }
}
