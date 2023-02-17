/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.util.URIs;

import java.net.URI;

/**
 *
 */
public class PathFormats {
    public static final String manifestPathFormat = "lot=%s/manifest.%s";

    public static String createManifestPath(String lotId, String extension) {
        return String.format(manifestPathFormat, lotId, extension);
    }

    public static URI createS3URI(String bucket, String key) {
        return URIs.create("s3", bucket, key);
    }
}
