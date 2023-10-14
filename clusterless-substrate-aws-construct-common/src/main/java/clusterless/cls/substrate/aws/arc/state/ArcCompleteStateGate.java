/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc.state;

import clusterless.aws.lambda.arc.ArcStateProps;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.naming.Label;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.props.LambdaJavaRuntimeProps;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.iam.IGrantable;

public class ArcCompleteStateGate extends ArcStateGate {
    public ArcCompleteStateGate(@NotNull ManagedComponentContext context, Arc<?> arc, ArcStateProps arcStateProps, LambdaJavaRuntimeProps runtimeProps) {
        super(context, Label.of("Complete"), arc, arcStateProps, "clusterless.aws.lambda.arc.ArcStateCompleteHandler", runtimeProps);
    }

    @Override
    protected void grantPermissionsTo(IGrantable grantable) {
        grantBootstrapReadWrite(grantable);
        grantEventBus(grantable);
    }
}
