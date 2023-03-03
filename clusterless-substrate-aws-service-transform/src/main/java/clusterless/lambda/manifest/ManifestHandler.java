/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.manifest;

import clusterless.lambda.StreamHandler;
import clusterless.lambda.transform.TransformProps;
import clusterless.model.manifest.Manifest;
import clusterless.substrate.aws.PathFormats;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.substrate.aws.sdk.EventBus;
import clusterless.substrate.aws.sdk.S3;
import clusterless.temporal.IntervalBuilder;
import clusterless.util.Env;
import clusterless.util.URIs;
import com.amazonaws.services.lambda.runtime.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Collection;

/**
 *
 */
public abstract class ManifestHandler<E> extends StreamHandler<E> {
    private static final Logger LOG = LogManager.getLogger(ManifestHandler.class);
    protected static final TransformProps transformProps = Env.fromEnv(
            TransformProps.class,
            () -> TransformProps.Builder.builder()
                    .build()
    );
    protected static final IntervalBuilder intervalBuilder = new IntervalBuilder(transformProps.lotUnit());
    protected static final S3 s3 = new S3();
    protected static final EventBus eventBus = new EventBus();

    public ManifestHandler(Class<E> type) {
        super(type);
    }

    @Override
    public void handleRequest(E event, Context context) {

        logObject("incoming event: {}", event);

        ManifestRequest request = new ManifestRequest();

        handleEvent(event, context, request);
    }

    protected abstract void handleEvent(E event, Context context, ManifestRequest request);

    protected ManifestRequest putManifest(Collection<URI> objects, ManifestRequest request) {
        Manifest manifest = Manifest.Builder.builder()
                .withDatasetName(transformProps.datasetName())
                .withDatasetVersion(transformProps.datasetVersion())
                .withDatasetPrefix(transformProps.datasetPrefix())
                .withLot(request.lotId())
                .build();

        for (URI object : objects) {
            manifest.datasetItems().add(object.toString());
        }

        request.setDatasetItemsSize(manifest.datasetItems().size());

        // put manifest, nested under the 'lot' partition
        URI manifestURI = URIs.copyAppendPath(transformProps.manifestPrefix(), PathFormats.createManifestPath(request.lotId(), manifest.extension()));

        request.setManifestURI(manifestURI);

        // todo: perform a listing to test for states (completed, empty, etc)
        S3.Response exists = s3.exists(manifestURI);

        if (exists.isSuccess()) {
            String message = String.format("manifest already exists: %s, having lot: %s", manifestURI, request.lotId());
            LOG.error(message);
            throw new ManifestExistsException(message);
        }

        LOG.info("writing {} to path: {}", () -> manifest.getClass().getSimpleName(), () -> manifestURI);

        S3.Response response = s3.put(manifestURI, manifest.contentType(), manifest);

        if (!response.isSuccess()) {
            String message = String.format("unable to write object: %s, %s", manifestURI, response.errorMessage());
            LOG.error(message, response.errorMessage());

            throw new RuntimeException(message, response.exception());
        }

        return request;
    }

    protected void publishEvent(ManifestRequest request) {
        // publish notification on event-bus
        ArcNotifyEvent notifyEvent = ArcNotifyEvent.Builder.builder()
                .withDatasetName(transformProps.datasetName())
                .withDatasetVersion(transformProps.datasetVersion())
                .withDatasetPrefix(transformProps.datasetPrefix())
                .withLotId(request.lotId())
                .withManifestURI(request.manifestURI())
                .build();

        LOG.info("publishing {} on {}", () -> notifyEvent.getClass().getSimpleName(), transformProps::eventBusName);

        EventBus.Response response = eventBus.put(transformProps.eventBusName(), notifyEvent);

        if (!response.isSuccess()) {
            String message = String.format("unable to publish event: %s, %s", transformProps.eventBusName(), response.errorMessage());
            LOG.error(message, response.errorMessage());

            throw new RuntimeException(message, response.exception());
        }
    }
}
