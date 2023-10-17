/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc;

import clusterless.cls.managed.component.ArcComponent;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.substrate.aws.managed.ManagedApp;
import clusterless.cls.substrate.aws.managed.ManagedNestedStack;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ArcWorkloadStack extends ManagedNestedStack implements ArcComponent {
    public ArcWorkloadStack(@NotNull ManagedApp managedApp, Arc arc) {
        super(managedApp, arc.label().with(arc.name()));


    }
}
