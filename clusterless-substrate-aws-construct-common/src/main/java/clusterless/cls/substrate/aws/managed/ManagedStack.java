/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.managed;

import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.substrate.aws.resources.Stacks;
import clusterless.commons.naming.Label;
import clusterless.commons.naming.Ref;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;


/**
 *
 */
public class ManagedStack extends ScopedStack implements Managed {
    private final ManagedApp managedApp;
    private final Deployable deployable;

    public ManagedStack(@NotNull ManagedApp managedApp, @NotNull Deployable deployable, @NotNull Label baseId) {
        this(Stacks.stackName(deployable, baseId), managedApp, deployable, baseId);
    }

    public ManagedStack(@NotNull Label stackName, @NotNull ManagedApp managedApp, @NotNull Deployable deployable, @NotNull Label baseId) {
        super(managedApp, baseId.camelCase(),
                StackProps.builder()
                        .env(Stacks.environmentFor(deployable))
                        .stackName(stackName.lowerHyphen())
                        .build()
        );

        this.managedApp = managedApp.addStack(this);
        this.deployable = deployable;
    }

    public ManagedApp managedProject() {
        return managedApp;
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

    @Override
    protected Ref withContext(Ref ref) {
        return super.withContext(ref)
                .withScope(managedProject().name())
                .withScopeVersion(managedProject().version());
    }
}
