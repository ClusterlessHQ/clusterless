/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.lambda.transform.LotSource;
import clusterless.model.deploy.IngressBoundary;
import clusterless.substrate.aws.props.LambdaJavaRuntimeProps;

/**
 *
 */
public class S3PutListenerBoundary extends IngressBoundary {
    LambdaJavaRuntimeProps runtimeProps = new LambdaJavaRuntimeProps(
            LambdaJavaRuntimeProps.MEM_1_024MB,
            3,
            15
    );

    LotSource lotSource = LotSource.objectModifiedTime;

    String keyRegex;

    public S3PutListenerBoundary() {
    }

    public LambdaJavaRuntimeProps runtimeProps() {
        return runtimeProps;
    }

    public LotSource lotSource() {
        return lotSource;
    }

    public String keyRegex() {
        return keyRegex;
    }
}
