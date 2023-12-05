/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.sdk;

import clusterless.cls.util.URIs;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateExportTaskRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateExportTaskResponse;

import java.net.URI;
import java.time.Instant;

public class CloudWatchLogs extends ClientBase<CloudWatchLogsClient> {

    public CloudWatchLogs() {
    }

    public CloudWatchLogs(String profile) {
        super(profile);
    }

    public CloudWatchLogs(String profile, String region) {
        super(profile, region);
    }

    @Override
    protected @NotNull String getEndpointEnvVar() {
        return "AWS_CLOUDWATCHLOGS_ENDPOINT";
    }

    @Override
    protected CloudWatchLogsClient createClient(String region) {
        logEndpointOverride();

        return CloudWatchLogsClient.builder()
                .region(region == null ? null : Region.of(region)) // allows sdk to lookup region in chain
                .credentialsProvider(credentialsProvider)
                .endpointOverride(endpointOverride)
                .build();
    }

    public Response createExportLogGroupTask(String taskName, String logGroupName, String logStreamPrefix, URI destination, Instant from, Instant to) {
        CreateExportTaskRequest request = CreateExportTaskRequest.builder()
                .taskName(taskName)
                .logGroupName(logGroupName)
                .from(from.toEpochMilli())
                .to(to.toEpochMilli())
                .destination(destination.getHost())
                .destinationPrefix(URIs.asKeyPrefix(destination))
                .logStreamNamePrefix(logStreamPrefix)
                .build();

        try (CloudWatchLogsClient client = createClient(region)) {
            return new Response(client.createExportTask(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public String exportTask(Response response) {
        return ((CreateExportTaskResponse) response.awsResponse()).taskId();
    }
}
