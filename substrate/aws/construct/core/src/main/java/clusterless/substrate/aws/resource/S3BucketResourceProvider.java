/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource;

import clusterless.managed.component.Component;
import clusterless.managed.component.ComponentService;
import clusterless.managed.component.ComponentType;
import clusterless.managed.component.ProvidesComponent;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(type = ComponentType.Resource, name = "core:s3Bucket")
public class S3BucketResourceProvider implements ComponentService<ManagedComponentContext, S3BucketResource> {

    @Override
    public Component create(ManagedComponentContext context, S3BucketResource model) {
        return new S3BucketConstruct(context.project(), "bucket" + model.bucketName());
    }

    @Override
    public Class<S3BucketResource> modelType() {
        return S3BucketResource.class;
    }
}
