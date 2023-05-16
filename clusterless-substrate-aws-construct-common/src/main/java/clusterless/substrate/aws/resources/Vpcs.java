/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.naming.Ref;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Fn;
import software.constructs.Construct;

import java.util.Objects;

public class Vpcs {

    public static final String COMMON_VPC = "CommonVpc";
    public static final String VPC = "vpc";

    public static String bootstrapVPCName(Construct scope) {
        Objects.requireNonNull(scope, "scope is null");

        return Resources.regionallyUniqueName(scope, COMMON_VPC);
    }

    public static String bootstrapVpcIdRef(@NotNull Construct scope) {
        return importValue(scope, Ref.idRef());
    }

    public static String bootstrapVpcArnRef(@NotNull Construct scope) {
        return importValue(scope, Ref.arnRef());
    }

    @NotNull
    private static String importValue(@NotNull Construct scope, Ref qualified) {
        Ref ref = ClsBootstrap.bootstrapBase(scope, qualified)
                .withResourceType(VPC)
                .withResourceName(COMMON_VPC);

        return Fn.importValue(ref.exportName());
    }
}
