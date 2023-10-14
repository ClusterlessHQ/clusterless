/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.workload.batch;

import clusterless.aws.lambda.arc.ArcEventHandler;
import clusterless.aws.lambda.arc.ArcEventObserver;
import clusterless.cls.model.deploy.WorkloadProps;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.substrate.aws.event.ArcWorkloadContext;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.ManifestURI;
import clusterless.cls.util.URIs;
import com.amazonaws.services.lambda.runtime.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BatchResultHandler extends ArcEventHandler<WorkloadProps> {
    private static final Logger LOG = LoggerFactory.getLogger(BatchResultHandler.class);
    protected static final S3 s3 = new S3();

    @Override
    protected Map<String, URI> handleEvent(ArcWorkloadContext workloadContext, Context context, ArcEventObserver eventObserver) {
        // sink role -> sink manifest
        Map<String, URI> result = new LinkedHashMap<>();

        String lot = workloadContext.arcNotifyEvent().lot();

        logInfo("handling lot: {}", lot);

        Map<String, ManifestURI> manifestPaths = arcProps().sinkManifestTemplates();

        for (Map.Entry<String, ManifestURI> entry : manifestPaths.entrySet()) {
            // find successful manifests
            String role = entry.getKey();
            ManifestURI manifestURI = entry.getValue();

            for (ManifestState state : List.of(ManifestState.complete, ManifestState.empty)) {
                URI uri = manifestURI
                        .withLot(lot)
                        .withState(state)
                        .uri();

                LOG.info("checking for manifest: {}", uri);

                S3.Response response = s3.listThisOrChildObjects(uri);

                List<String> children = s3.listChildren(response);
                if (!children.isEmpty()) {
                    Optional<String> first = children.stream().filter(child -> child.endsWith(".json")).findFirst();

                    if (first.isEmpty()) {
                        logErrorAndThrow(RuntimeException::new, "no json manifest found in: {}", children.stream().limit(5).collect(Collectors.joining(",")));
                    }

                    result.put(role, URIs.copyWith(uri, first.orElseThrow()));
                    eventObserver.applyToManifest(role, uri);
                    break;
                }
            }

            if (result.containsKey(role)) {
                break;
            }

            // find partial manifests that may have attempts
            URI partial = manifestURI
                    .withLot(lot)
                    .withState(ManifestState.partial)
                    .uriPrefix();

            S3.Response response = s3.listObjects(partial);

            List<String> children = s3.listChildren(response);

            if (!children.isEmpty()) {
                logErrorAndThrow(RuntimeException::new, "partial result are currently unsupported, got: {}", children.stream().limit(5).collect(Collectors.joining(",")));
            }
        }

        return result;
    }
}
