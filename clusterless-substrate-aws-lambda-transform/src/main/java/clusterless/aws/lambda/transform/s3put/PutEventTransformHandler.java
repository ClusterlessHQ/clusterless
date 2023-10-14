/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.transform.s3put;

import clusterless.aws.lambda.EventHandler;
import clusterless.aws.lambda.arc.ArcNotifyEventPublisher;
import clusterless.aws.lambda.manifest.ManifestWriter;
import clusterless.aws.lambda.transform.json.object.AWSEvent;
import clusterless.aws.lambda.util.PathMatcher;
import clusterless.cls.model.UriType;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.temporal.IntervalBuilder;
import clusterless.cls.util.Env;
import com.amazonaws.services.lambda.runtime.Context;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class PutEventTransformHandler extends EventHandler<AWSEvent, PutEventTransformObserver> {
    private static final Logger LOG = LogManager.getLogger(PutEventTransformHandler.class);
    protected static final S3 s3 = new S3();

    protected final S3PutTransformProps transformProps = Env.fromEnv(
            S3PutTransformProps.class,
            () -> S3PutTransformProps.builder()
                    .build()
    );
    protected final IntervalBuilder intervalBuilder = new IntervalBuilder(transformProps.lotUnit());

    protected ManifestWriter manifestWriter = new ManifestWriter(
            transformProps.manifestCompletePath(),
            UriType.identifier
    );

    protected ArcNotifyEventPublisher arcNotifyEventPublisher = new ArcNotifyEventPublisher(
            transformProps.eventBusName(),
            transformProps.dataset()
    );

    protected PathMatcher pathMatcher = PathMatcher.builder()
            .withPath(transformProps.dataset().pathURI().getPath())
            .withPathSeparator(transformProps.filter().pathSeparator())
            .withIgnoreCase(transformProps.filter().ignoreCase())
            .withIncludes(transformProps.filter().includes())
            .withExcludes(transformProps.filter().excludes())
            .build();

    public PutEventTransformHandler() {
        super(AWSEvent.class);
    }

    protected PutEventTransformObserver observer() {
        return new PutEventTransformObserver() {
            @Override
            public void applyLotId(String lotId) {
                LOG.info("using lot: {}", lotId);
            }

            @Override
            public void applyDatasetItemsSize(int datasetItemsSize) {
            }

            @Override
            public void applyManifestURI(URI manifestURI) {
                LOG.info("using manifest uri: {}", manifestURI);
            }

            @Override
            public void applyEvent(OffsetDateTime time, String bucket, String key) {
                LOG.info("received, time: {}, bucket: {}, key: {}", time, bucket, key);
            }

            @Override
            public void applyIdentifierURI(URI identifierURI) {
                LOG.info("using identifier uri: {}", identifierURI);
            }
        };
    }

    @Override
    public void handleEvent(AWSEvent event, Context context, PutEventTransformObserver eventObserver) {
        OffsetDateTime time = event.getTime();
        String bucket = event.getDetail().getBucket().getName();
        String key = event.getDetail().getObject().getKey();

        eventObserver.applyEvent(time, bucket, key);

        if (key.endsWith("/")) {
            LOG.info("key ends with a path separator, skipping: {}", key);
            return;
        }

        URI identifier = S3.createS3URI(bucket, key);

        eventObserver.applyIdentifierURI(identifier);

        String lotId =
                switch (transformProps.lotSource()) {
                    case eventTime -> intervalBuilder.truncateAndFormat(time);
                    case objectModifiedTime -> fromModifiedTime(identifier);
                    case keyTimestampRegex -> null;
                };

        eventObserver.applyLotId(lotId);

        List<URI> uris = Collections.emptyList();

        if (pathMatcher.keep(key)) {
            uris = List.of(identifier);
        }

        eventObserver.applyDatasetItemsSize(uris.size());

        URI manifestURI = uris.isEmpty() ?
                manifestWriter.writeEmptyManifest(lotId) : manifestWriter.writeSuccessManifest(uris, lotId);

        eventObserver.applyManifestURI(manifestURI);

        arcNotifyEventPublisher.publishEvent(lotId, manifestURI);
    }

    private String fromModifiedTime(URI objectPath) {
        S3.Response response = s3.exists(objectPath);

        if (!s3.exists(response)) {
            throw new IllegalStateException("object not found: " + objectPath, response.exception());
        }

        return intervalBuilder.truncateAndFormat(s3.lastModified(response));
    }
}
