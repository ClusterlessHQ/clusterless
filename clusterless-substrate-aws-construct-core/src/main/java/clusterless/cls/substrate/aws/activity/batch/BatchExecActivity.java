/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.activity.batch;

import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.model.deploy.Activity;
import clusterless.cls.substrate.aws.props.BatchRuntimeProps;

import java.net.URI;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class BatchExecActivity extends Activity {
    BatchRuntimeProps batchRuntimeProps = new BatchRuntimeProps();
    @JsonRequiredProperty
    String computeEnvironmentRef;
    @JsonRequiredProperty
    Path imagePath;
    Map<String, String> imageBuildArgs = new LinkedHashMap<>();
    boolean imageBuildCacheEnabled = true;
    String imageExtraHash;
    Map<String, String> environment = new LinkedHashMap<>();
    List<String> command = new LinkedList<>();
    String bucketRef;
    @JsonRequiredProperty
    URI pathURI;

    public BatchRuntimeProps batchRuntimeProps() {
        return batchRuntimeProps;
    }

    public String computeEnvironmentRef() {
        return computeEnvironmentRef;
    }

    public Path imagePath() {
        return imagePath;
    }

    public Map<String, String> imageBuildArgs() {
        return imageBuildArgs;
    }

    public boolean imageBuildCacheEnabled() {
        return imageBuildCacheEnabled;
    }

    public String imageExtraHash() {
        return imageExtraHash;
    }

    public Map<String, String> environment() {
        return environment;
    }

    public List<String> command() {
        return command;
    }

    public String bucketRef() {
        return bucketRef;
    }

    public URI pathURI() {
        return pathURI;
    }
}
