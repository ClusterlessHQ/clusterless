/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc;

import clusterless.aws.lambda.arc.ArcStateProps;
import clusterless.cls.naming.Label;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.managed.ManagedStateMachineFragment;
import clusterless.cls.substrate.aws.resources.BootstrapStores;
import clusterless.cls.substrate.aws.resources.Events;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.events.IEventBus;
import software.amazon.awscdk.services.iam.IGrantable;

public abstract class ArcStateMachineFragment extends ManagedStateMachineFragment {
    protected final ArcStateProps arcStateProps;

    public ArcStateMachineFragment(@NotNull ManagedComponentContext context, Label baseId, ArcStateProps arcStateProps) {
        super(context, baseId);
        this.arcStateProps = arcStateProps;
    }

    public ArcStateProps arcStateProps() {
        return arcStateProps;
    }

    protected abstract void grantPermissionsTo(IGrantable grantable);

    protected void grantEventBus(IGrantable grantable) {
        String eventBusRef = Events.arcEventBusNameRef(this);
        IEventBus arcEventBus = EventBus.fromEventBusName(this, "EventBus", eventBusRef);
        arcEventBus.grantPutEventsTo(grantable);
    }

    protected void grantBootstrapReadWrite(@NotNull IGrantable grantee) {
        BootstrapStores.arcStateBucket(this).grantReadWrite(grantee);
        BootstrapStores.manifestBucket(this).grantReadWrite(grantee);
    }
}
