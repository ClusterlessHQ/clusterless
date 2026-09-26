/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
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
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

import java.util.stream.Collectors;

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
            PutEventsResponse response = eventBridgeClient.putEvents(request);

            // PutEvents reports rejected entries in-band with a successful http status
            if (response.failedEntryCount() != null && response.failedEntryCount() > 0) {
                return new Response(new IllegalStateException(failedEntriesMessage(eventBusName, response)));
            }

            return new Response(response);
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    private static String failedEntriesMessage(String eventBusName, PutEventsResponse response) {
        String failures = response.entries().stream()
                .filter(entry -> entry.errorCode() != null)
                .map(entry -> "%s: %s".formatted(entry.errorCode(), entry.errorMessage()))
                .collect(Collectors.joining(", "));

        return "event bus: %s, rejected %d of %d events: %s".formatted(eventBusName, response.failedEntryCount(), response.entries().size(), failures);
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
