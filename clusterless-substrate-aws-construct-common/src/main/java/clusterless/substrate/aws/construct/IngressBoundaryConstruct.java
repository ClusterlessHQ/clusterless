/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.construct;

import clusterless.managed.component.BoundaryComponent;
import clusterless.model.deploy.IngressBoundary;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class IngressBoundaryConstruct<M extends IngressBoundary> extends ModelConstruct<M> implements BoundaryComponent {
    public IngressBoundaryConstruct(@NotNull ManagedComponentContext context, @NotNull M model) {
        super(context, model, model.name());
    }

    public IngressBoundaryConstruct(@NotNull ManagedComponentContext context, @NotNull M model, @NotNull String id) {
        super(context, model, id);
    }
}
