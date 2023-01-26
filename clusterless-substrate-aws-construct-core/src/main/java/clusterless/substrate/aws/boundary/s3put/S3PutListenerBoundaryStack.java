/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.managed.Label;
import clusterless.managed.component.Component;
import clusterless.substrate.aws.boundary.BoundaryStack;
import clusterless.substrate.aws.managed.ManagedProject;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.StackProps;

/**
 *
 */
public class S3PutListenerBoundaryStack extends BoundaryStack implements Component {
    public S3PutListenerBoundaryStack(@Nullable ManagedProject scope, Label id, @Nullable StackProps props) {
        super(scope, id, props);

        new S3PutListenerConstruct(this, "");
    }
}
