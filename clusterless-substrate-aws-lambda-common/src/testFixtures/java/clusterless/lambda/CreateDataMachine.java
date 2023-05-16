/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda;

import clusterless.model.UriType;
import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.SourceDataset;
import clusterless.model.manifest.Manifest;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.sdk.S3;
import clusterless.substrate.aws.uri.ManifestURI;
import clusterless.util.URIs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class CreateDataMachine {
    private static final Logger LOG = LogManager.getLogger(CreateDataMachine.class);

    S3 s3 = new S3();

    List<String> lots = new LinkedList<>();


    public CreateDataMachine(String... lots) {
        this(Arrays.asList(lots));
    }

    public CreateDataMachine(List<String> lots) {
        this.lots.addAll(lots);
    }

    public CreateDataMachine applyBucketsFrom(Map<String, ? extends Dataset> datasetMap) {

        Set<String> buckets = datasetMap.values().stream()
                .map(Dataset::pathURI).map(URI::getHost).collect(Collectors.toSet());

        buckets.forEach(this::createBucket);

        return this;
    }

    private void createBucket(String bucketName) {
        LOG.info("creating bucket: {}", bucketName);

        S3.Response response = s3.create(bucketName);

        response.isSuccessOrThrowRuntime(
                r -> String.format("unable to create bucket: %s, %s", bucketName, r.errorMessage())
        );
    }

    public CreateDataMachine buildSources(Map<String, ManifestURI> manifestMap, Map<String, SourceDataset> sourceMap) {
        for (String lot : lots) {
            for (Map.Entry<String, SourceDataset> entry : sourceMap.entrySet()) {
                String role = entry.getKey();
                SourceDataset dataset = entry.getValue();

                URI datasetPath = dataset.pathURI();

                URI dataIdentifier = URIs.copyAppend(datasetPath, String.format("lot=%s", lot), "data.csv");

                LOG.info("writing data for: {}", dataIdentifier);

                S3.Response dataResponse = s3.put(dataIdentifier, "application/text", String.format("role=%s,lot=%s", role, lot));

                dataResponse.isSuccessOrThrowRuntime(
                        r -> String.format("unable to write data: %s, %s", dataIdentifier, r.errorMessage())
                );

                Manifest manifest = Manifest.builder()
                        .withUris(List.of(dataIdentifier))
                        .withDataset(new Dataset(dataset)) // don't serialize a subclass
                        .withLotId(lot)
                        .withUriType(UriType.identifier)
                        .build();

                ManifestURI manifestPath = manifestMap.get(role);
                URI manifestIdentifier = manifestPath.withState(ManifestState.complete).withLot(lot).uri();

                LOG.info("writing manifest for: {}", manifestIdentifier);

                S3.Response manifestResponse = s3.put(manifestIdentifier, manifest.contentType(), manifest);

                manifestResponse.isSuccessOrThrowRuntime(
                        r -> String.format("unable to write manifest: %s, %s", manifestIdentifier, r.errorMessage())
                );
            }
        }

        return this;
    }
}
