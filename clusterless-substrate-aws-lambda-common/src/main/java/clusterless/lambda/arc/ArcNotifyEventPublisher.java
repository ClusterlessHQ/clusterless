/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.SinkDataset;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.substrate.aws.sdk.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ArcNotifyEventPublisher {
    private static final Logger LOG = LogManager.getLogger(ArcNotifyEventPublisher.class);
    protected static final EventBus eventBus = new EventBus();
    private final String eventBusName;
    private final Dataset dataset;

    public ArcNotifyEventPublisher(String eventBusName, Dataset dataset) {
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

        LOG.info("publishing {} on {}", () -> notifyEvent.getClass().getSimpleName(), () -> eventBusName);

        EventBus.Response response = eventBus.put(eventBusName, notifyEvent);

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to publish event: %s, %s", eventBusName, r.errorMessage())
        );
    }
}
