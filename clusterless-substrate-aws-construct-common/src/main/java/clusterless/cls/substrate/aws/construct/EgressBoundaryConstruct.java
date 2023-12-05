/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.construct;

import clusterless.cls.managed.component.BoundaryComponent;
import clusterless.cls.model.deploy.EgressBoundary;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public abstract class EgressBoundaryConstruct<M extends EgressBoundary> extends ExtensibleConstruct<M> implements BoundaryComponent {
    public EgressBoundaryConstruct(@NotNull ManagedComponentContext context, @NotNull M model) {
        super(context, model);
    }
}
