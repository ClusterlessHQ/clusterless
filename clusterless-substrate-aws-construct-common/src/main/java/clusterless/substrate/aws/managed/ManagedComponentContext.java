/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.config.Configurations;
import clusterless.managed.component.ComponentContext;
import clusterless.managed.dataset.DatasetResolver;
import clusterless.model.deploy.Deployable;
import software.constructs.Construct;

/**
 *
 */
public class ManagedComponentContext implements ComponentContext {

    final Configurations configurations;
    final DatasetResolver resolver;
    final ManagedProject managedProject;
    final Deployable deployable;
    final Managed parent;

    public ManagedComponentContext(Configurations configurations, DatasetResolver resolver, ManagedProject managedProject, Deployable deployable) {
        this(configurations, resolver, managedProject, deployable, managedProject);
    }

    public ManagedComponentContext(Configurations configurations, DatasetResolver resolver, ManagedProject managedProject, Deployable deployable, Managed parent) {
        this.configurations = configurations;
        this.resolver = resolver;
        this.managedProject = managedProject;
        this.deployable = deployable;
        this.parent = parent;
    }

    public Configurations configurations() {
        return configurations;
    }

    public DatasetResolver resolver() {
        return resolver;
    }

    public ManagedProject managedProject() {
        return managedProject;
    }

    public Deployable deployable() {
        return deployable;
    }

    public Construct parentConstruct() {
        return parent.asConstruct();
    }
}
