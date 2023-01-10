/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import org.jetbrains.annotations.NotNull;
import software.constructs.Construct;

/**
 *
 */
public class S3PutListenerConstruct extends Construct {
    public S3PutListenerConstruct(@NotNull Construct scope, @NotNull String id) {
        super(scope, id);
    }
}
