/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource;

import clusterless.model.Resource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates and maintains an S3 bucket and any associated metadata.
 */
public class S3BucketResource extends Resource {
    String bucketName;
    boolean versioned = false;
    Map<String, String> tags = new LinkedHashMap<>();

    public S3BucketResource() {
    }

    public String bucketName() {
        return bucketName;
    }

    public boolean versioned() {
        return versioned;
    }

    public Map<String, String> tags() {
        return tags;
    }
}
