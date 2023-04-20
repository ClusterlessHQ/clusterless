/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.naming.Label;
import org.jetbrains.annotations.NotNull;
import software.constructs.Construct;

/**
 *
 */
public class ManagedConstruct extends Construct implements Managed {
    private final ManagedComponentContext context;

    public ManagedConstruct(@NotNull ManagedComponentContext context, Label baseId) {
        super(context.parentConstruct(), baseId.camelCase());
        this.context = context;
    }

    public ManagedComponentContext context() {
        return context;
    }

}
