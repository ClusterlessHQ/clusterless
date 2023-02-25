/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.lambda.transform.LotSource;
import clusterless.model.Struct;
import clusterless.model.deploy.IngressBoundary;

/**
 *
 */
public class S3PutListenerBoundary extends IngressBoundary {
    public static class RuntimeProps implements Struct {
        int memorySizeMB = 256 * 3;

        int retryAttempts = 3;

        int timeoutMin = 15;

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

    RuntimeProps runtimeProps = new RuntimeProps();

    LotSource lotSource = LotSource.objectModifiedTime;

    String keyRegex;

    public S3PutListenerBoundary() {
    }

    public RuntimeProps runtimeProps() {
        return runtimeProps;
    }

    public LotSource lotSource() {
        return lotSource;
    }

    public String keyRegex() {
        return keyRegex;
    }
}
