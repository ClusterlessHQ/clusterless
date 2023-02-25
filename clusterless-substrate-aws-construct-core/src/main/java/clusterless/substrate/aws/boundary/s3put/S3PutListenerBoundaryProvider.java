/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.managed.component.ComponentService;
import clusterless.managed.component.Isolation;
import clusterless.managed.component.ModelType;
import clusterless.managed.component.ProvidesComponent;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(provides = ModelType.Boundary, name = "aws:core:s3PutListenerBoundary", isolation = Isolation.included)
public class S3PutListenerBoundaryProvider implements ComponentService<ManagedComponentContext, S3PutListenerBoundary, S3PutListenerBoundaryConstruct> {
    @Override
    public S3PutListenerBoundaryConstruct create(ManagedComponentContext context, S3PutListenerBoundary boundary) {
        return new S3PutListenerBoundaryConstruct(context, boundary);
    }

    @Override
    public Class<S3PutListenerBoundary> modelClass() {
        return S3PutListenerBoundary.class;
    }
}
