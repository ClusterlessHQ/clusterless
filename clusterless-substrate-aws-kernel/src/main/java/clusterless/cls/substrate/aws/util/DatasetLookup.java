/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.util;

import clusterless.cls.json.JSONUtil;
import clusterless.cls.managed.dataset.DatasetResolver;
import clusterless.cls.managed.dataset.RemoteDatasetOwnerLookup;
import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.DatasetURI;
import com.fasterxml.jackson.core.type.TypeReference;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DatasetLookup {
    private static final Logger LOG = LoggerFactory.getLogger(DatasetLookup.class);

    @NotNull
    public static DatasetResolver createResolver(Boolean resolveDeployedDatasets, String profile, List<Deployable> deployables) {
        return createResolver(resolveDeployedDatasets, profile, deployables, Collections.emptyList());
    }

    @NotNull
    public static DatasetResolver createResolver(Boolean resolveDeployedDatasets, String profile, List<Deployable> deployables, List<Deployable> resolveables) {
        if (!resolveDeployedDatasets) {
            return new DatasetResolver(deployables, resolveables);
        }

        return new DatasetResolver(
                deployables,
                resolveables,
                getRemoteDatasetOwnerLookup(profile)
        );
    }

    public static @NotNull RemoteDatasetOwnerLookup getRemoteDatasetOwnerLookup(String profile) {
        return (placement, source) -> {
            S3 s3 = new S3(profile, placement.region());

            URI datasetURI = DatasetURI.builder()
                    .withPlacement(placement)
                    .withDataset(source)
                    .build()
                    .uri();

            LOG.info("looking up dataset at: {}", datasetURI);

            S3.Response response = s3.get(datasetURI);

            if (!s3.exists(response)) {
                response.isSuccessOrLog(r -> {
                    if (r.isAccessDenied() || r.isMissingCredentials()) {
                        return String.format("access denied to dataset, ensure profile is set via --profile or AWS_PROFILE: %s, %s", datasetURI, r.errorMessage());
                    } else {
                        return String.format("dataset lookup failed at: %s, status: %d, error: %s, %s", datasetURI, r.statusCode(), r.errorCode(), r.errorMessage());
                    }
                });

                return Optional.empty();
            }

            return Optional.of(JSONUtil.readAsObjectSafe(response.asInputStream(), new TypeReference<>() {
            }));
        };
    }
}
