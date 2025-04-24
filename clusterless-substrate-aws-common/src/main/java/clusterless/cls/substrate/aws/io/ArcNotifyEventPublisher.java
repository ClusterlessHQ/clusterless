/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.io;

import clusterless.cls.model.deploy.SinkDataset;
import clusterless.cls.substrate.aws.event.ArcNotifyEvent;
import clusterless.cls.substrate.aws.sdk.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArcNotifyEventPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(ArcNotifyEventPublisher.class);
    private final EventBus eventBus = new EventBus();
    private final String eventBusName;
    private final SinkDataset dataset;

    public ArcNotifyEventPublisher(String eventBusName, SinkDataset dataset) {
        this.eventBusName = eventBusName;
        this.dataset = dataset;
    }

    public static Map<String, ArcNotifyEventPublisher> publishers(String eventBusName, Map<String, SinkDataset> datasets) {
        return datasets.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new ArcNotifyEventPublisher(eventBusName, e.getValue())));
    }

    public void publishEvent(String lotId, URI manifestURI) {
        Objects.requireNonNull(lotId, "lotId may not be null");
        Objects.requireNonNull(manifestURI, "manifestURI may not be null");

        // publish notification on event-bus
        ArcNotifyEvent notifyEvent = ArcNotifyEvent.Builder.builder()
                .withDataset(dataset)
                .withLot(lotId)
                .withManifest(manifestURI)
                .build();

        if (!dataset.publish()) {
            LOG.atInfo()
                    .setMessage("skipping publish of {} on {}")
                    .addArgument(() -> notifyEvent.getClass().getSimpleName())
                    .addArgument(() -> eventBusName)
                    .log();
            return;
        }

        LOG.atInfo()
                .setMessage("publishing {} on {}")
                .addArgument(() -> notifyEvent.getClass().getSimpleName())
                .addArgument(() -> eventBusName)
                .log();

        EventBus.Response response = eventBus.put(eventBusName, notifyEvent);

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to publish event: %s, %s", eventBusName, r.errorMessage())
        );
    }
}
