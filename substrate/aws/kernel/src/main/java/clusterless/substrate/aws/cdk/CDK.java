/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;

import clusterless.managed.component.ComponentProps;
import clusterless.managed.component.ComponentService;
import clusterless.managed.component.ComponentServices;
import clusterless.managed.component.ComponentType;
import clusterless.substrate.aws.managed.ManagedComponentProps;
import clusterless.substrate.aws.managed.ManagedProject;

import java.util.Optional;

/**
 *
 */
public class CDK {
    public static void main(String[] args) {
        ManagedProject project = new ManagedProject();

        ComponentServices componentServices = new ComponentServices();

        ManagedComponentProps props = new ManagedComponentProps(project);

        System.out.println("componentServices = " + componentServices.names(ComponentType.Boundary));

        Optional<ComponentService<ComponentProps>> boundary = componentServices.get(ComponentType.Boundary, "S3PutListenerBoundary");

        // create all the stacks within the current namespace

//        project.synth();
    }
}
