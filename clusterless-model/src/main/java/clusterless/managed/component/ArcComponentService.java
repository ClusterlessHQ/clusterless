/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.component;

import clusterless.managed.ModelType;
import clusterless.model.deploy.Arc;

/**
 *
 */
@DeclaresComponent(
        provides = ModelType.Arc,
        isolation = Isolation.managed
)
public interface ArcComponentService<CC extends ComponentContext, M extends Arc, C extends ArcComponent> extends ComponentService<CC, M, C> {
}