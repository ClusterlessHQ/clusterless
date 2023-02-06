/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.managed.component.ComponentContext;
import clusterless.model.Deploy;
import software.constructs.Construct;

/**
 *
 */
public class ManagedComponentContext implements ComponentContext {

    final ManagedProject managedProject;
    final Deploy deploy;
    final Managed parent;

    public ManagedComponentContext(ManagedProject managedProject, Deploy deploy) {
        this.managedProject = managedProject;
        this.parent = managedProject;
        this.deploy = deploy;
    }

    public ManagedComponentContext(ManagedProject managedProject, Deploy deploy, Managed parent) {
        this.managedProject = managedProject;
        this.deploy = deploy;
        this.parent = parent;
    }

    public ManagedProject managedProject() {
        return managedProject;
    }

    public Deploy project() {
        return deploy;
    }

    public Construct parent() {
        return parent.asConstruct();
    }
}
