/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.s3;

import clusterless.model.Resource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates and maintains an S3 bucket and any associated metadata.
 */
public class S3BucketResource extends Resource {
    private String bucketName;
    private boolean versioned = false;

    /**
     * When true (the default) the bucket and it's data will be removed when the project is destroyed.
     */
    boolean removeOnDestroy = true;
    Map<String, String> tags = new LinkedHashMap<>();

    public S3BucketResource() {
    }

    public String bucketName() {
        return bucketName;
    }

    public boolean versioned() {
        return versioned;
    }

    public boolean removeOnDestroy() {
        return removeOnDestroy;
    }

    public Map<String, String> tags() {
        return tags;
    }
}
