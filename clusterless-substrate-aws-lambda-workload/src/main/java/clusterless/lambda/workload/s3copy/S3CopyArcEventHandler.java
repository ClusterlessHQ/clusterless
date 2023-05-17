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
import clusterless.substrate.aws.event.ArcWorkloadContext;
import clusterless.substrate.aws.sdk.ClientBase;
import clusterless.substrate.aws.sdk.S3;
import clusterless.util.Tuple2;
import clusterless.util.Tuple3;
import clusterless.util.URIs;
import com.amazonaws.services.lambda.runtime.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class S3CopyArcEventHandler extends ArcEventHandler<S3CopyProps> {
    private static final Logger LOG = LogManager.getLogger(S3CopyArcEventHandler.class);

    protected static final S3 s3 = new S3();

    protected ManifestReader manifestReader = new ManifestReader();

    protected Map<String, ManifestWriter> manifestWriters = ManifestWriter.writers(
            arcProps().sinks(),
            arcProps().sinkManifestPaths(),
            UriType.identifier
    );

    @Override
    protected Map<String, URI> handleEvent(ArcWorkloadContext arcWorkloadContext, Context context, ArcEventObserver eventObserver) {
        String fromRole = arcWorkloadContext.role();
        ArcNotifyEvent notifyEvent = arcWorkloadContext.arcNotifyEvent();
        String lotId = notifyEvent.lot();
        URI incomingManifestIdentifier = notifyEvent.manifest();

        Manifest incomingManifest = manifestReader.getManifest(incomingManifestIdentifier);

        eventObserver.applyFromManifest(incomingManifest);

        Map<String, URI> result = new LinkedHashMap<>();

        //  copy files
        URI fromDatasetPath = incomingManifest.dataset().pathURI();
        List<URI> fromUris = incomingManifest.uris();

        for (Map.Entry<String, SinkDataset> sinkRoleEntry : arcProps().sinks().entrySet()) {

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
            List<Tuple3<URI, URI, S3.Response>> failed = new LinkedList<>();

            int maxAllowedFailures = (int) Math.ceil(toUris.size() * workloadProperties().failArcOnPartialPercent());

            s3.copy(
                    toUris,
                    completed::add,
                    (tuple, response) -> {
                        failed.add(new Tuple3<>(tuple.get_1(), tuple.get_2(), response));

                        if (response.isAccessDenied()) {
                            return true;
                        }

                        return failed.size() > maxAllowedFailures;
                    }
            );

            ManifestWriter manifestWriter = manifestWriters.get(toRole);

            URI manifestURI;

            if (!failed.isEmpty()) { // unintentional
                LOG.error("s3 object copy errors: {}, failed: {}", fromUris.size(), failed.size());

                Tuple3<URI, URI, ClientBase<S3Client>.Response> firstFailure = failed.get(0);
                if (firstFailure.get_3().isAccessDenied()) {
                    logErrorAndThrow(
                            RuntimeException::new,
                            firstFailure.get_3().exception(),
                            "bucket access denied, may not have access to either: {}, or: {}, confirm arc has permission to read from subscribed dataset buckets",
                            firstFailure.get_1().getHost(), // from bucket
                            firstFailure.get_2().getHost() // to bucket
                    );
                }

                Set<String> messages = new LinkedHashSet<>();
                for (Tuple3<URI, URI, S3.Response> failure : failed) {
                    messages.add(failure.get_3().exception().getMessage());
                    LOG.error("failed from: {}, to: {}, message: {}", failure.get_1(), failure.get_2(), failure.get_3().exception().getMessage(), failure.get_3().exception());
                }

                if (failed.size() > maxAllowedFailures) {
                    logErrorAndThrow(
                            RuntimeException::new,
                            "too many copy operations returned errors, failed: {}, is greater than allowed: {}, with message: {}",
                            failed.size(),
                            maxAllowedFailures,
                            messages.stream().findFirst().orElse("[no error message]")
                    );
                }

                List<String> errors = messages.stream().limit(3).collect(Collectors.toList());

                manifestURI = manifestWriter.writePartialManifest(completed, lotId, String.format("copy failed on role: %s, num: %d with messages: %s", toRole, failed.size(), errors));
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
