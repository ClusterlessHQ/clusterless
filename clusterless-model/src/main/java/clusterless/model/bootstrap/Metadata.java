/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.bootstrap;

import clusterless.model.Struct;

/**
 *
 */
public class Metadata implements Struct {
    String stage;
    String version;
    String arcStateBucketName;
    String manifestBucketName;

    public Metadata() {
    }

    public Metadata(String stage, String version, String arcStateBucketName, String manifestBucketName) {
        this.version = version;
        this.arcStateBucketName = arcStateBucketName;
        this.manifestBucketName = manifestBucketName;
    }

    public String stage() {
        return stage;
    }

    public String version() {
        return version;
    }

    public String arcStateBucketName() {
        return arcStateBucketName;
    }

    public String manifestBucketName() {
        return manifestBucketName;
    }
}
