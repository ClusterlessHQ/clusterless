/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.s3;

import clusterless.managed.component.ProvidesComponent;
import clusterless.managed.component.ResourceComponentService;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent("aws:core:s3Bucket")
public class S3BucketResourceProvider implements ResourceComponentService<ManagedComponentContext, S3BucketResource, S3BucketResourceConstruct> {

    @Override
    public S3BucketResourceConstruct create(ManagedComponentContext context, S3BucketResource model) {
        return new S3BucketResourceConstruct(context, model);
    }

    @Override
    public Class<S3BucketResource> modelClass() {
        return S3BucketResource.class;
    }
}
