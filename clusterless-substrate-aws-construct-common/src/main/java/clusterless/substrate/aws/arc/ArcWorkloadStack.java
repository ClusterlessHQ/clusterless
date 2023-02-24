/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc;

import clusterless.model.deploy.Workload;
import clusterless.substrate.aws.managed.ManagedNestedStack;
import clusterless.substrate.aws.managed.ManagedProject;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ArcWorkloadStack extends ManagedNestedStack {
    public ArcWorkloadStack(@NotNull ManagedProject managedProject, Workload workload) {
        super(managedProject, workload.label().with(workload.name()));


    }
}
