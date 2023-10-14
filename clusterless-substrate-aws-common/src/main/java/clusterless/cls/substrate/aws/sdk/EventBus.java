/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.sdk;

import clusterless.cls.json.JSONUtil;
import clusterless.cls.substrate.aws.event.NotifyEvent;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.CreateEventBusRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

/**
 *
 */
public class EventBus extends ClientBase<EventBridgeClient> {
    public EventBus() {
    }

    @NotNull
    protected String getEndpointEnvVar() {
        return "AWS_EVENTS_ENDPOINT";
    }

    @Override
    protected EventBridgeClient createClient(String region) {
        logEndpointOverride();

        return EventBridgeClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .endpointOverride(endpointOverride)
                .build();
    }

    public Response put(String eventBusName, NotifyEvent event) {
        return put(eventBusName, event.eventSource(), event.eventDetail(), event);
    }

    public Response put(String eventBusName, String source, String detailType, Object event) {
        String detail = JSONUtil.writeAsStringSafe(event);

        PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                .eventBusName(eventBusName)
                .source(source)
                .detailType(detailType)
                .detail(detail)
                .build();

        PutEventsRequest request = PutEventsRequest.builder()
                .entries(entry)
                .build();

        try (EventBridgeClient eventBridgeClient = createClient()) {
            return new Response(eventBridgeClient.putEvents(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response create(String eventBusName) {
        CreateEventBusRequest request = CreateEventBusRequest.builder()
                .name(eventBusName)
                .build();

        try (EventBridgeClient eventBridgeClient = createClient()) {
            return new Response(eventBridgeClient.createEventBus(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }
}
