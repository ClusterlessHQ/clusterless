/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.eventbridge;

import clusterless.cls.managed.component.ResourceComponent;
import clusterless.cls.naming.Label;
import clusterless.cls.substrate.aws.construct.ModelConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.events.EventBus;

/**
 *
 */
public class EventBridgeResourceConstruct extends ModelConstruct<EventBridgeResource> implements ResourceComponent {

    private final EventBus eventBus;

    public EventBridgeResourceConstruct(@NotNull ManagedComponentContext context, @NotNull EventBridgeResource model) {
        super(context, model, model.eventBusName());

        eventBus = EventBus.Builder.create(this, Label.of(model.eventBusName()).camelCase())
                .eventBusName(model.eventBusName())
                .build();

        addArnRefFor(model(), eventBus(), eventBus().getEventBusArn(), "event bus arn");
        addNameRefFor(model(), eventBus(), model().eventBusName(), "event bus name");
    }

    public EventBus eventBus() {
        return eventBus;
    }
}
