/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.naming.Ref;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.ec2.VpcLookupOptions;
import software.constructs.Construct;

import java.util.Objects;

public class Vpcs {
    private static final Logger LOG = LoggerFactory.getLogger(Vpcs.class);

    public static final String COMMON_VPC = "CommonVpc";
    public static final String VPC = "vpc";

    public static @NotNull IVpc lookupVpc(Construct scope, @NotNull ManagedComponentContext context) {
        // need to be careful where we look up region, as it may not be set yet
        return lookupVpc(scope, context.deployable().placement().region());
    }

    public static @NotNull IVpc lookupVpc(Construct scope, String region) {
        String vpcName = Vpcs.bootstrapVPCName(scope);
        LOG.info("looking up vpc: {}, in region: {}", vpcName, region);

        IVpc vpcLookup = Vpc.fromLookup(
                scope,
                "VpcLookup",
                VpcLookupOptions.builder()
                        .region(region)
                        .vpcName(vpcName)
                        .build()
        );

        LOG.info("found vpc id: {}", vpcLookup.getVpcId());

        return vpcLookup;
    }

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
