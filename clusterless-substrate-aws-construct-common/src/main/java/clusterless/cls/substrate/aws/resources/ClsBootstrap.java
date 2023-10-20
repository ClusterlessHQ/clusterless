/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import clusterless.commons.naming.Ref;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedApp;
import org.jetbrains.annotations.NotNull;
import software.constructs.Construct;

public class ClsBootstrap {
    public static final String BOOTSTRAP = "bootstrap";
    public static final String BOOTSTRAP_VERSION = "1";

    @NotNull
    public static Ref bootstrapBase(@NotNull Construct scope, Ref qualified) {
        return qualified.withProvider("aws")
                .withStage(ScopedApp.scopedOf(scope).stage())
                .withScope(BOOTSTRAP)
                .withScopeVersion(BOOTSTRAP_VERSION)
                .withResourceNs("meta");
    }
}
