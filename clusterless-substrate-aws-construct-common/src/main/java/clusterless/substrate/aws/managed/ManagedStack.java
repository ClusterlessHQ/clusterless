/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.model.deploy.Deployable;
import clusterless.substrate.aws.resources.Stacks;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

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
                        .env(environment(deployable))
                        .stackName(stackName.lowerHyphen())
                        .build()
        );

        this.managedProject = managedProject.addStack(this);
        this.deployable = deployable;
    }

    private static Environment environment(Deployable deployable) {
        return Environment.builder()
                .account(deployable.placement().account())
                .region(deployable.placement().region())
                .build();
    }

    public ManagedProject managedProject() {
        return managedProject;
    }

    public Deployable project() {
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
