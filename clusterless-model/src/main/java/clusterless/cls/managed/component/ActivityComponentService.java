/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.managed.component;

import clusterless.cls.managed.ModelType;
import clusterless.cls.model.deploy.Activity;
import clusterless.cls.model.deploy.Placement;

/**
 *
 */
@DeclaresComponent(
        provides = ModelType.Activity,
        isolation = Isolation.grouped
)
public interface ActivityComponentService<CC extends ComponentContext, M extends Activity, C extends ActivityComponent> extends ComponentService<CC, M, C> {
    default ActivityLocalExecutor executor(Placement placement, Activity activity) {
        throw new UnsupportedOperationException("local exec is unsupported by this activity :" + activity.type());
    }

}
