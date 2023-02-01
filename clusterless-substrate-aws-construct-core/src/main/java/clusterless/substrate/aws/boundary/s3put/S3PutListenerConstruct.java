/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.managed.component.Component;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.model.ModelConstruct;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class S3PutListenerConstruct extends ModelConstruct<S3PutListenerBoundary> implements Component {
    public S3PutListenerConstruct(@NotNull ManagedComponentContext context, @NotNull S3PutListenerBoundary model) {
        super(context, model, model.bucketName());

        // declare listen bucket
        // declare manifest bucket
        // declare event bridge
        // declare lambda to convert put event into arc event

    }
}
