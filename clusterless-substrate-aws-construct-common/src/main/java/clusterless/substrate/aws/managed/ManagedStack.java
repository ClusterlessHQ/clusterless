/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.model.deploy.Deployable;
import clusterless.naming.Label;
import clusterless.substrate.aws.resources.Stacks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import static clusterless.substrate.aws.resources.Stacks.environmentFor;


/**
 *
 */
public class ManagedStack extends StagedStack implements Managed {
    private final ManagedProject managedProject;
    private final Deployable deployable;

    public ManagedStack(@NotNull ManagedProject managedProject, @NotNull Deployable deployable, @NotNull Label baseId) {
        this(Stacks.stackName(deployable, baseId), managedProject, deployable, baseId);
    }

    public ManagedStack(@NotNull Label stackName, @NotNull ManagedProject managedProject, @NotNull Deployable deployable, @NotNull Label baseId) {
        super(managedProject, baseId.camelCase(),
                StackProps.builder()
                        .env(environmentFor(deployable))
                        .stackName(stackName.lowerHyphen())
                        .build()
        );

        this.managedProject = managedProject.addStack(this);
        this.deployable = deployable;
    }

    public ManagedProject managedProject() {
        return managedProject;
    }

    public Deployable deployable() {
        return deployable;
    }

    @Override
    public void addDependency(@NotNull Stack target, @Nullable String reason) {
        if (target != this) {
            super.addDependency(target, reason);
        }
    }

    @Override
    public void addDependency(@NotNull Stack target) {
        if (target != this) {
            super.addDependency(target);
        }
    }
}
