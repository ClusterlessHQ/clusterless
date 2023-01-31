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
import clusterless.managed.component.ComponentService;
import clusterless.managed.component.ModelType;
import clusterless.managed.component.ProvidesComponent;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import software.amazon.awscdk.StackProps;

/**
 *
 */
@ProvidesComponent(modelType = ModelType.Boundary, name = "S3PutListenerBoundary")
public class S3PutListenerBoundaryProvider implements ComponentService<ManagedComponentContext, S3PutListenerBoundary> {
    @Override
    public Component create(ManagedComponentContext context, S3PutListenerBoundary boundary) {
        StackProps stackProps = StackProps.builder()
                .build();

        return new S3PutListenerBoundaryStack(context.managedProject(), context.project(), Label.of("stack"), stackProps);
    }

    @Override
    public Class<S3PutListenerBoundary> modelClass() {
        return S3PutListenerBoundary.class;
    }

}
