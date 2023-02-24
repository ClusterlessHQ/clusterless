/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.s3copy;

import clusterless.substrate.aws.construct.WorkloadConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class S3CopyWorkloadConstruct extends WorkloadConstruct<S3CopyWorkload> {
    public S3CopyWorkloadConstruct(@NotNull ManagedComponentContext context, @NotNull S3CopyWorkload model) {
        super(context, model);
    }
}
