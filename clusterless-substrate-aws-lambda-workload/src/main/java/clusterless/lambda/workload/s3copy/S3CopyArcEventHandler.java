/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.workload.s3copy;

import clusterless.lambda.arc.ArcEventHandler;
import clusterless.lambda.arc.ArcEventObserver;
import clusterless.lambda.manifest.ManifestReader;
import clusterless.lambda.manifest.ManifestWriter;
import clusterless.model.UriType;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.manifest.Manifest;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.substrate.aws.sdk.S3;
import clusterless.util.URIs;
import com.amazonaws.services.lambda.runtime.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class S3CopyArcEventHandler extends ArcEventHandler {
    private static final Logger LOG = LogManager.getLogger(S3CopyArcEventHandler.class);

    protected static final S3 s3 = new S3();

    ManifestReader manifestReader = new ManifestReader();

    protected Map<String, ManifestWriter> manifestWriters = ManifestWriter.writers(
            arcProps.sinks(),
            arcProps.sinkManifestPaths(),
            UriType.identifier
    );

    @Override
    protected ArcEventObserver observer() {
        return new ArcEventObserver() {
            @Override
            public void applyManifest(Manifest manifest) {
                String name = manifest.dataset().name();
                String version = manifest.dataset().version();
                String lotId = manifest.lotId();
                int size = manifest.uris().size();
                LOG.info("manifest from dataset name: {}, version: {}, lot: {}, size: {}", name, version, lotId, size);
            }

            @Override
            public void applyToDataset(String role, SinkDataset sinkDataset) {
                String name = sinkDataset.name();
                String version = sinkDataset.version();
                URI pathURI = sinkDataset.pathURI();
                LOG.info("writing to dataset name: {}, version: {}, with role: {} at {}", name, version, role, pathURI);
            }

        };
    }

    @Override
    protected void handleEvent(ArcNotifyEvent event, Context context, ArcEventObserver eventObserver) {
        String lotId = event.lotId();
        URI manifest = event.manifest();

        Manifest incomingManifest = manifestReader.getManifest(manifest);

        eventObserver.applyManifest(incomingManifest);

        //  copy files
        URI fromDatasetPath = incomingManifest.dataset().pathURI();
        List<URI> fromUris = incomingManifest.uris();

        for (Map.Entry<String, URI> entry : arcProps.sinkManifestPaths().entrySet()) {
            List<URI> results = new LinkedList<>();

            String role = entry.getKey();
            SinkDataset sinkDataset = arcProps.sinks().get(role);
            eventObserver.applyToDataset(role, sinkDataset);

            URI toDatasetPath = sinkDataset.pathURI();
            for (URI fromUri : fromUris) {
                URI toURI = URIs.fromTo(fromDatasetPath, fromUri, toDatasetPath);

                results.add(toURI);

                S3.Response copyResponse = s3.copy(fromUri, toURI);

                if (!copyResponse.isSuccess()) {
                    String message = String.format("unable to copy object: %s, %s", fromUri, copyResponse.errorMessage());
                    LOG.error(message, copyResponse.errorMessage());

                    throw new RuntimeException(message, copyResponse.exception());
                }
            }

            manifestWriters.get(role).putManifest(results, lotId);
        }
    }
}
