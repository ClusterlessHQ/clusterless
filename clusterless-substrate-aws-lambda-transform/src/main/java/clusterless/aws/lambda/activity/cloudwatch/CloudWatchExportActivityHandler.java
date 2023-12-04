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
import clusterless.cls.substrate.aws.sdk.CloudWatchLogs;
import clusterless.cls.util.Env;
import clusterless.commons.temporal.IntervalBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    CloudWatchLogs cloudWatchLogs = new CloudWatchLogs();

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
        URI path = activityProps.destinationURI();

        if (!eventObserver.enableExport()) {
            LOG.info("export disabled");
            return;
        }

        getStopwatch.start();
        CloudWatchLogs.Response response = cloudWatchLogs.createExportLogGroupTask(taskName, logGroupName, logStreamPrefix, path, startTimeInclusive, endTimeInclusive);
        getStopwatch.stop();

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to create export task: %s, %s, %s", logGroupName, path, r.errorMessage())
        );

        Duration getElapsed = getStopwatch.elapsed();
        LOG.info("export task: {}, duration: {}", cloudWatchLogs.exportTask(response), getElapsed);
    }
}
