/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.managed.component.BoundaryAnnotation;
import clusterless.managed.component.ComponentFactory;
import clusterless.managed.component.ComponentService;

/**
 *
 */
@BoundaryAnnotation
public class S3PutListenerBoundaryProvider implements ComponentService {
    @Override
    public String name() {
        return "S3PutListenerBoundary";
    }

    @Override
    public ComponentFactory getFactory() {
        return new S3PutListenerBoundaryFactory();
    }
}
