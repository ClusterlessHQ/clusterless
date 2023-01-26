/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.managed.Label;
import org.jetbrains.annotations.NotNull;
import software.constructs.Construct;

/**
 *
 */
public class ManagedConstruct extends Construct implements Managed {
    private final ManagedComponentContext context;
    private final Label baseId;

    public ManagedConstruct(@NotNull ManagedComponentContext context, @NotNull Label baseId) {
        super(context.parent(), baseId.camelCase());
        this.context = context;
        this.baseId = baseId;
    }

    public ManagedComponentContext context() {
        return context;
    }

    public Label baseId() {
        return baseId;
    }
}
