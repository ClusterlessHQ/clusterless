/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;

import clusterless.managed.component.*;
import clusterless.model.Model;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedProject;

import java.util.Optional;

/**
 *
 */
public class CDK {
    public static void main(String[] args) {
        ManagedProject managedProject = new ManagedProject();

        ComponentServices componentServices = ComponentServices.INSTANCE;

        ManagedComponentContext props = new ManagedComponentContext(managedProject);

        System.out.println("componentServices = " + componentServices.names(ComponentType.Boundary));

        Optional<ComponentService<ComponentContext, Model>> boundary = componentServices.get(ComponentType.Boundary, "S3PutListenerBoundary");

        Component component = boundary.orElseThrow().create(props, null);

        // create all the stacks within the current namespace

        managedProject.synth();
    }
}
