/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.managed.component.*;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(modelType = ModelType.Boundary, name = "core:s3PutListenerBoundary", managedType = ManagedType.member)
public class S3PutListenerBoundaryProvider implements ComponentService<ManagedComponentContext, S3PutListenerBoundary> {
    @Override
    public Component create(ManagedComponentContext context, S3PutListenerBoundary boundary) {
        return new S3PutListenerConstruct(context, boundary);
    }

    @Override
    public Class<S3PutListenerBoundary> modelClass() {
        return S3PutListenerBoundary.class;
    }
}
