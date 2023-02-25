/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.eventbridge;

import clusterless.managed.component.ComponentService;
import clusterless.managed.component.ModelType;
import clusterless.managed.component.ProvidesComponent;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(provides = ModelType.Resource, name = "aws:core:eventBus")
public class EventBridgeResourceProvider implements ComponentService<ManagedComponentContext, EventBridgeResource, EventBridgeModelConstruct> {

    @Override
    public EventBridgeModelConstruct create(ManagedComponentContext context, EventBridgeResource model) {
        return new EventBridgeModelConstruct(context, model);
    }

    @Override
    public Class<EventBridgeResource> modelClass() {
        return EventBridgeResource.class;
    }
}
