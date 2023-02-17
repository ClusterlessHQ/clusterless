/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.lambda.manifest.ManifestHandler;
import clusterless.lambda.manifest.ManifestRequest;
import clusterless.lambda.transform.json.AWSEvent;
import clusterless.substrate.aws.PathFormats;
import clusterless.substrate.aws.sdk.S3;
import com.amazonaws.services.lambda.runtime.Context;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

/**
 *
 */
public class PutEventTransformHandler extends ManifestHandler<AWSEvent> {

    public PutEventTransformHandler() {
        super(AWSEvent.class);
    }

    @Override
    public void handleEvent(AWSEvent event, Context context, ManifestRequest request) {
        OffsetDateTime time = event.getTime();
        String bucket = event.getDetail().getBucket().getName();
        String key = event.getDetail().getObject().getKey();

        logMessage("received, time: {}, bucket: {}, key: {}", time, bucket, key);

        URI objectPath = PathFormats.createS3URI(bucket, key);

        String lotId = null;

        switch (transformProps.lotSource()) {
            case eventTime:
                lotId = intervalBuilder.truncateAndFormat(time);
                break;
            case objectModifiedTime:
                S3.Response response = s3.exists(objectPath);

                if (!s3.exists(response)) {
                    throw new IllegalStateException("object not found: " + objectPath, response.exception());
                }

                lotId = intervalBuilder.truncateAndFormat(s3.lastModified(response));
                break;
            case keyTimestampRegex:
                lotId = null;
                break;
        }

        request.setLotId(lotId);

        URI manifestURI = putManifest(lotId, List.of(objectPath), request);

        publishEvent(lotId, manifestURI, request);
    }

}
