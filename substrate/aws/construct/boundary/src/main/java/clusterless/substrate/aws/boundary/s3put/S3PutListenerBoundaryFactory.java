/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.managed.component.Component;
import clusterless.managed.component.ComponentFactory;
import clusterless.substrate.aws.managed.ManagedComponentProps;
import software.amazon.awscdk.StackProps;

/**
 *
 */
public class S3PutListenerBoundaryFactory implements ComponentFactory<ManagedComponentProps> {
    @Override
    public Component create(ManagedComponentProps componentProps) {
        StackProps stackProps = StackProps.builder()
                .build();

        return new S3PutListenerBoundaryStack(componentProps.project(), "", stackProps);
    }
}
