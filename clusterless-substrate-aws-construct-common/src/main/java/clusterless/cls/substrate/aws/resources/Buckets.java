/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

public class Buckets {
    /**
     * <a href="https://docs.aws.amazon.com/AmazonS3/latest/userguide/bucketnamingrules.html">Naming Rules</a>
     *
     * @param bucketName
     * @return
     */
    public static String verifyBucketName(String bucketName) {
        // try to be helpful, then be strict
        if (bucketName.length() > 63) {
            throw new IllegalStateException("bucket name too long, must be < 64 characters, got: " + bucketName);
        }

        if (!bucketName.equals(bucketName.toLowerCase())) {
            throw new IllegalStateException("bucket name may not contain uppercase characters, got: " + bucketName);
        }

        if (bucketName.contains("..")) {
            throw new IllegalStateException("bucket name may not contain two adjacent periods, got: " + bucketName);
        }

        if (!bucketName.matches("^[0-9a-z].*")) {
            throw new IllegalStateException("bucket name must start with a letter or number, got: " + bucketName);
        }

        if (!bucketName.matches(".*[0-9a-z]$")) {
            throw new IllegalStateException("bucket name must end with a letter or number, got: " + bucketName);
        }

        if (!bucketName.matches("(?!(^((2(5[0-5]|[0-4][0-9])|[01]?[0-9]{1,2})\\.){3}(2(5[0-5]|[0-4][0-9])|[01]?[0-9]{1,2})$|^xn--|.+-s3alias$))^[a-z0-9][a-z0-9.-]{1,61}[a-z0-9]$")) {
            throw new IllegalStateException("bucket name is not valid, got: " + bucketName);
        }

        return bucketName;
    }
}
