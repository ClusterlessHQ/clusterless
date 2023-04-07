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
import clusterless.substrate.aws.event.ArcStateContext;
import clusterless.substrate.aws.sdk.S3;
import clusterless.util.Tuple2;
import clusterless.util.Tuple3;
import clusterless.util.URIs;
import com.amazonaws.services.lambda.runtime.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
public class S3CopyArcEventHandler extends ArcEventHandler<S3CopyProps> {
    private static final Logger LOG = LogManager.getLogger(S3CopyArcEventHandler.class);

    protected static final S3 s3 = new S3();

    protected ManifestReader manifestReader = new ManifestReader();

    protected Map<String, ManifestWriter> manifestWriters = ManifestWriter.writers(
            arcProps.sinks(),
            arcProps.sinkManifestPaths(),
            UriType.identifier
    );

    @Override
    protected ArcEventObserver observer() {
        return new ArcEventObserver() {
            @Override
            public void applyFromManifest(Manifest manifest) {
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

            @Override
            public void applyToManifest(String role, URI manifest) {
                LOG.info("write manifest: {}, with role: {}", manifest, role);
            }
        };
    }

    @Override
    protected Map<String, URI> handleEvent(ArcStateContext arcStateContext, Context context, ArcEventObserver eventObserver) {
        String fromRole = arcStateContext.role();
        ArcNotifyEvent notifyEvent = arcStateContext.arcNotifyEvent();
        String lotId = notifyEvent.lotId();
        URI incomingManifestIdentifier = notifyEvent.manifest();

        Manifest incomingManifest = manifestReader.getManifest(incomingManifestIdentifier);

        eventObserver.applyFromManifest(incomingManifest);

        Map<String, URI> result = new LinkedHashMap<>();

        //  copy files
        URI fromDatasetPath = incomingManifest.dataset().pathURI();
        List<URI> fromUris = incomingManifest.uris();

        for (Map.Entry<String, SinkDataset> sinkRoleEntry : arcProps.sinks().entrySet()) {

            String toRole = sinkRoleEntry.getKey();
            SinkDataset sinkDataset = sinkRoleEntry.getValue();

            eventObserver.applyToDataset(toRole, sinkDataset);

            URI toDatasetPath = sinkDataset.pathURI();

            // not using a map so that collisions can be managed independently on the to/from sides
            List<Tuple2<URI, URI>> toUris = new LinkedList<>();
            for (URI fromUri : fromUris) {
                URI toURI = URIs.fromTo(fromDatasetPath, fromUri, toDatasetPath);
                toUris.add(new Tuple2<>(fromUri, toURI));
            }

            // todo: check integrity of uris to be copied

            List<URI> completed = new LinkedList<>();
            List<Tuple3<URI, URI, String>> failed = new LinkedList<>();

            s3.copy(
                    toUris,
                    completed::add,
                    (tuple, response) -> {
                        failed.add(new Tuple3<>(tuple.get_1(), tuple.get_2(), response.errorMessage()));
                        return failed.size() >= 5;
                    }
            );

            ManifestWriter manifestWriter = manifestWriters.get(toRole);

            URI manifestURI;

            if (!failed.isEmpty()) { // unintentional
                LOG.error("s3 object copy errors: {}, failed: {}", fromUris.size(), failed.size());
                for (Tuple3<URI, URI, String> failure : failed) {
                    LOG.error("failed from: {}, to: {}, message: {}", failure.get_1(), failure.get_2(), failure.get_3());
                }

                int ceil = (int) Math.ceil(toUris.size() * workloadProperties().failArcOnPartialPercent());
                if (failed.size() >= ceil) {
                    logErrorAndThrow(
                            RuntimeException::new,
                            "too many copy operations returned errors, failed: {}, is greater than allowed: {}", failed.size(), ceil
                    );
                }

                List<String> errors = failed.stream().map(Tuple3::get_3).distinct().limit(3).collect(Collectors.toList());

                manifestURI = manifestWriter.writePartialManifest(completed, lotId, String.format("copy failed on role: %s, num: %d with: %s", toRole, failed.size(), errors));
            } else if (completed.isEmpty()) { // intentional
                // if we allow for a filter predicate on the declaration, this will be a valid state
                LOG.info("no objects copied with no errors, role: {} -> {}, having: {}", fromRole, toRole, fromUris.size());
                manifestURI = manifestWriter.writeEmptyManifest(completed, lotId);
            } else { // intentional
                LOG.info("successfully copied objects with no errors, role: {} -> {}, having: {}", fromRole, toRole, fromUris.size());
                manifestURI = manifestWriter.writeSuccessManifest(completed, lotId);
            }

            result.put(toRole, manifestURI);

            eventObserver.applyToManifest(toRole, manifestURI);
        }

        return result;
    }
}
