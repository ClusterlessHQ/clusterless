/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.eventbridge;

import clusterless.managed.component.ResourceComponent;
import clusterless.substrate.aws.construct.ModelConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.services.events.EventBus;

import java.util.Locale;

/**
 *
 */
public class EventBridgeModelConstruct extends ModelConstruct<EventBridgeResource> implements ResourceComponent {

    private final EventBus eventBus;

    public EventBridgeModelConstruct(@NotNull ManagedComponentContext context, @NotNull EventBridgeResource model) {
        super(context, model, model.eventBusName());

        eventBus = EventBus.Builder.create(this, Label.of(model.eventBusName()).camelCase())
                .eventBusName(model.eventBusName())
                .build();

        new CfnOutput(this, id("EventBusARN"), new CfnOutputProps.Builder()
                .exportName(String.format("eventbus:%s:arn", model().eventBusName().toLowerCase(Locale.ROOT)))
                .value(eventBus().getEventBusArn())
                .description("event bus arn")
                .build());

        new CfnOutput(this, id("EventBusName"), new CfnOutputProps.Builder()
                .exportName(String.format("eventbus:%s:name", model().eventBusName().toLowerCase(Locale.ROOT)))
                .value(eventBus().getEventBusName())
                .description("event bus name")
                .build());
    }

    public EventBus eventBus() {
        return eventBus;
    }
}
