/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary;

import clusterless.managed.Label;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.ManagedStack;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.StackProps;

/**
 *
 */
public class BoundaryStack extends ManagedStack {
    public BoundaryStack(@NotNull ManagedProject scope, @NotNull Label id, @NotNull StackProps props) {
        super(scope, id, props);
    }
}
