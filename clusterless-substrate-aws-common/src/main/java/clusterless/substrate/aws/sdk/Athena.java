/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.sdk;

import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.athena.AthenaClient;

/**
 * SELECT * FROM "<table_name>$partitions" to get count
 */
public class Athena extends ClientBase<AthenaClient> {
    public Athena() {
    }

    public Athena(String profile) {
        super(profile);
    }

    public Athena(String profile, String region) {
        super(profile, region);
    }

    @NotNull
    protected String getEndpointEnvVar() {
        return "AWS_ATHENA_ENDPOINT";
    }

    @Override
    protected AthenaClient createClient(String region) {
        logEndpointOverride();

        return AthenaClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .endpointOverride(endpointOverride)
                .build();
    }
}
