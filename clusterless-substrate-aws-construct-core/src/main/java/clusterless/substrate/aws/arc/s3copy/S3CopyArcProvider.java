/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.s3copy;

import clusterless.managed.component.ArcComponentService;
import clusterless.managed.component.ProvidesComponent;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent("aws:core:s3CopyArc")
public class S3CopyArcProvider implements ArcComponentService<ManagedComponentContext, S3CopyArc, S3CopyArcConstruct> {

    @Override
    public S3CopyArcConstruct create(ManagedComponentContext context, S3CopyArc model) {
        return new S3CopyArcConstruct(context, model);
    }

    @Override
    public Class<S3CopyArc> modelClass() {
        return S3CopyArc.class;
    }
}
