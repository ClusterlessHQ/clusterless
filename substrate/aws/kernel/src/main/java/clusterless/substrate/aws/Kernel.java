/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.managed.component.BoundaryAnnotation;
import clusterless.managed.component.ComponentService;
import clusterless.substrate.aws.managed.ManagedComponentProps;
import clusterless.substrate.aws.managed.ManagedProject;

import java.util.ServiceLoader;

/**
 *
 */
public class Kernel {
    public static void main(String[] args) {
        ManagedProject project = new ManagedProject();

        ServiceLoader<ComponentService> componentServices = ServiceLoader.load(ComponentService.class);

        componentServices.stream()
                .filter(p -> p.type().isAnnotationPresent(BoundaryAnnotation.class))
                .map(ServiceLoader.Provider::get)
                .forEach(s -> System.out.println(s.name()));

        ManagedComponentProps props = new ManagedComponentProps(project);

        // create all the stacks within the current namespace

//        project.synth();
    }
}
