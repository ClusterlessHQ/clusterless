/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.s3copy;

import clusterless.managed.component.ComponentService;
import clusterless.managed.component.ModelType;
import clusterless.managed.component.ProvidesComponent;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(provides = ModelType.Workload, name = "core:s3Copy")
public class S3CopyWorkloadProvider implements ComponentService<ManagedComponentContext, S3CopyWorkload, S3CopyWorkloadConstruct> {

    @Override
    public S3CopyWorkloadConstruct create(ManagedComponentContext context, S3CopyWorkload model) {
        return null;
    }

    @Override
    public Class<S3CopyWorkload> modelClass() {
        return S3CopyWorkload.class;
    }
}
