/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.boundary.s3put;

import clusterless.json.JsonRequiredProperty;
import clusterless.lambda.transform.s3put.EventArrival;
import clusterless.model.deploy.IngressBoundary;
import clusterless.model.deploy.partial.PathFilter;
import clusterless.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.substrate.aws.props.Memory;

/**
 *
 */
public class S3PutListenerBoundary extends IngressBoundary {
    @JsonRequiredProperty
    EventArrival eventArrival = EventArrival.infrequent;
    LambdaJavaRuntimeProps runtimeProps = new LambdaJavaRuntimeProps(
            Memory.MEM_1_024MB,
            3,
            15
    );
    Infrequent infrequent = new Infrequent();
    Frequent frequent = new Frequent();

    PathFilter filter = new PathFilter();

    public S3PutListenerBoundary() {
    }

    public EventArrival eventArrival() {
        return eventArrival;
    }

    public LambdaJavaRuntimeProps runtimeProps() {
        return runtimeProps;
    }

    public Infrequent infrequent() {
        return infrequent;
    }

    public Frequent frequent() {
        return frequent;
    }

    public PathFilter filter() {
        return filter;
    }
}
