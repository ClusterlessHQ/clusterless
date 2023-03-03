/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.component;

import clusterless.model.deploy.Resource;

/**
 *
 */
@DeclaresComponent(
        provides = ModelType.Resource,
        isolation = Isolation.grouped
)
public interface ResourceComponentService<CC extends ComponentContext, M extends Resource, C extends ResourceComponent> extends ComponentService<CC, M, C> {
}
