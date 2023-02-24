/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.NestedStack;
import software.amazon.awscdk.NestedStackProps;
import software.amazon.awscdk.RemovalPolicy;

/**
 *
 */
public class ManagedNestedStack extends NestedStack implements Managed {

    public ManagedNestedStack(@NotNull ManagedProject managedProject, @NotNull Label baseId) {
        super(managedProject, baseId.camelCase(),
                NestedStackProps.builder()
                        .description("nested arc process stack")
                        .removalPolicy(RemovalPolicy.DESTROY)
                        .build()
        );
    }
}
