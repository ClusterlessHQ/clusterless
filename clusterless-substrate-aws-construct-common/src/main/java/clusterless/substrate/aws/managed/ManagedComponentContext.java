/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.managed.component.ComponentContext;
import clusterless.model.deploy.Deployable;
import software.constructs.Construct;

/**
 *
 */
public class ManagedComponentContext implements ComponentContext {

    final ManagedProject managedProject;
    final Deployable deployable;
    final Managed parent;

    public ManagedComponentContext(ManagedProject managedProject, Deployable deployable) {
        this.managedProject = managedProject;
        this.parent = managedProject;
        this.deployable = deployable;
    }

    public ManagedComponentContext(ManagedProject managedProject, Deployable deployable, Managed parent) {
        this.managedProject = managedProject;
        this.deployable = deployable;
        this.parent = parent;
    }

    public ManagedProject managedProject() {
        return managedProject;
    }

    public Deployable deployable() {
        return deployable;
    }

    public Construct parent() {
        return parent.asConstruct();
    }
}
