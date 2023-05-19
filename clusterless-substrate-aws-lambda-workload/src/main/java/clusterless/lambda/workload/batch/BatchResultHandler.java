/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.workload.batch;

import clusterless.lambda.arc.ArcEventHandler;
import clusterless.lambda.arc.ArcEventObserver;
import clusterless.model.deploy.WorkloadProps;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.event.ArcWorkloadContext;
import clusterless.substrate.aws.sdk.S3;
import clusterless.substrate.uri.ManifestURI;
import com.amazonaws.services.lambda.runtime.Context;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchResultHandler extends ArcEventHandler<WorkloadProps> {
    protected static final S3 s3 = new S3();

    @Override
    protected Map<String, URI> handleEvent(ArcWorkloadContext workloadContext, Context context, ArcEventObserver eventObserver) {
        // sink role -> sink manifest
        Map<String, URI> result = new LinkedHashMap<>();

        String lot = workloadContext.arcNotifyEvent().lot();

        logInfo("handling lot: {}", lot);

        Map<String, ManifestURI> manifestPaths = arcProps().sinkManifestPaths();

        for (Map.Entry<String, ManifestURI> entry : manifestPaths.entrySet()) {
            // find successful manifests
            String role = entry.getKey();
            ManifestURI manifestURI = entry.getValue();

            for (ManifestState state : List.of(ManifestState.complete, ManifestState.empty)) {
                URI uri = manifestURI
                        .withLot(lot)
                        .withState(state)
                        .uri();

                S3.Response response = s3.exists(uri);

                if (s3.exists(response)) {
                    result.put(role, uri);
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
