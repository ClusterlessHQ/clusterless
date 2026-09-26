/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.sdk;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * PutEvents reports rejected entries in-band, with a successful http status, so a put
 * must inspect the entry results rather than trust the call's status.
 */
public class EventBusTest {
    private static EventBus eventBusReturning(PutEventsResponse response) {
        EventBridgeClient client = mock(EventBridgeClient.class);

        when(client.putEvents(any(PutEventsRequest.class))).thenReturn(response);

        return new EventBus() {
            @Override
            protected EventBridgeClient createClient(String region) {
                return client;
            }
        };
    }

    private static PutEventsResponse response(int failedEntryCount, PutEventsResultEntry entry) {
        return (PutEventsResponse) PutEventsResponse.builder()
                .failedEntryCount(failedEntryCount)
                .entries(entry)
                .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                .build();
    }

    @Test
    void failedEntryIsNotSuccess() {
        EventBus eventBus = eventBusReturning(response(1, PutEventsResultEntry.builder()
                .errorCode("InternalFailure")
                .errorMessage("event bus unavailable")
                .build()));

        EventBus.Response response = eventBus.put("bus", "source", "detail-type", Map.of("key", "value"));

        Assertions.assertFalse(response.isSuccess());
        Assertions.assertTrue(response.errorMessage().contains("InternalFailure"), response.errorMessage());
        Assertions.assertThrows(RuntimeException.class, () -> response.isSuccessOrThrowRuntime(r -> "put failed"));
    }

    @Test
    void acceptedEntryIsSuccess() {
        EventBus eventBus = eventBusReturning(response(0, PutEventsResultEntry.builder()
                .eventId("event-1")
                .build()));

        EventBus.Response response = eventBus.put("bus", "source", "detail-type", Map.of("key", "value"));

        Assertions.assertTrue(response.isSuccess());
    }
}
