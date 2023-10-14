/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.eventbridge;

import clusterless.cls.managed.component.ProvidesComponent;
import clusterless.cls.managed.component.ResourceComponentService;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:eventBus",
        synopsis = "Create an AWS EventBridge EventBus.",
        description = """
                eventBusName: The unique name of the event bus to create
                """
)
public class EventBridgeResourceProvider implements ResourceComponentService<ManagedComponentContext, EventBridgeResource, EventBridgeResourceConstruct> {

    @Override
    public EventBridgeResourceConstruct create(ManagedComponentContext context, EventBridgeResource model) {
        return new EventBridgeResourceConstruct(context, model);
    }

    @Override
    public Class<EventBridgeResource> modelClass() {
        return EventBridgeResource.class;
    }
}
