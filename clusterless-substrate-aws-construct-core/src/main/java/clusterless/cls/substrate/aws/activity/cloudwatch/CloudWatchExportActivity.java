/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.activity.cloudwatch;

import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.model.deploy.Activity;
import clusterless.cls.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.cls.substrate.aws.props.Memory;

import java.net.URI;

/**
 * Creates and maintains an S3 bucket and any associated metadata.
 */
public class CloudWatchExportActivity extends Activity {
    @JsonRequiredProperty
    private String logGroupName;

    private String logStreamPrefix;

    @JsonRequiredProperty
    private URI destinationURI;

    LambdaJavaRuntimeProps runtimeProps = new LambdaJavaRuntimeProps(
            Memory.MEM_1_024MB,
            3,
            15
    );

    public CloudWatchExportActivity() {
    }

    public String logGroupName() {
        return logGroupName;
    }

    public String logStreamPrefix() {
        return logStreamPrefix;
    }

    public URI destinationURI() {
        return destinationURI;
    }

    public LambdaJavaRuntimeProps runtimeProps() {
        return runtimeProps;
    }
}
