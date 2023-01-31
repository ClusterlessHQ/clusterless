/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.managed.component.ComponentContext;
import clusterless.model.Project;
import software.constructs.Construct;

/**
 *
 */
public class ManagedComponentContext implements ComponentContext {

    ManagedProject managedProject;
    Managed parent;
    private Project project;

    public ManagedComponentContext(ManagedProject managedProject, Project project) {
        this.managedProject = managedProject;
        this.parent = managedProject;
        this.project = project;
    }

    public ManagedComponentContext(ManagedProject managedProject, Project project, Managed parent) {
        this.managedProject = managedProject;
        this.project = project;
        this.parent = parent;
    }

    public ManagedProject managedProject() {
        return managedProject;
    }

    public Project project() {
        return project;
    }

    public Construct parent() {
        return parent.asConstruct();
    }
}
