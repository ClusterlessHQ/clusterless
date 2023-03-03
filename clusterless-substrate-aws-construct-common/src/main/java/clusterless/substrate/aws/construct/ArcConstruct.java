/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.construct;

import clusterless.managed.component.ArcComponent;
import clusterless.model.deploy.Arc;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.stepfunctions.State;

/**
 *
 */
public abstract class ArcConstruct<M extends Arc> extends ModelConstruct<M> implements ArcComponent {
    public ArcConstruct(@NotNull ManagedComponentContext context, @NotNull M model) {
        super(context, model, model.name());
    }

    public abstract State createState();
}
