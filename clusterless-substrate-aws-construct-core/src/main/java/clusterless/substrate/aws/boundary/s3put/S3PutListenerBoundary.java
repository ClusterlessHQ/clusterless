/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.model.IngressBoundary;

import java.net.URI;

/**
 *
 */
public class S3PutListenerBoundary extends IngressBoundary {
    private URI listenBucketURI;
    private URI manifestBucketURI;
    private String eventBusName;
    private String listenerRuleName;

    int memorySizeMB = 256 * 3;

    int retryAttempts = 3;

    int timeoutMin = 15;

    public S3PutListenerBoundary() {
    }

    public URI listenBucketURI() {
        return listenBucketURI;
    }

    public URI manifestBucketURI() {
        return manifestBucketURI;
    }

    public String eventBusName() {
        return eventBusName;
    }

    public String listenerRuleName() {
        return listenerRuleName;
    }

    public int memorySizeMB() {
        return memorySizeMB;
    }

    public int retryAttempts() {
        return retryAttempts;
    }

    public int timeoutMin() {
        return timeoutMin;
    }
}
