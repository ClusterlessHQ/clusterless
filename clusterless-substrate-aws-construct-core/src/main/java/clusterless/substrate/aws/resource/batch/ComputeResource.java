/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.batch;

import clusterless.model.deploy.Resource;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates and maintains an S3 bucket and any associated metadata.
 */
public class ComputeResource extends Resource {
    public enum ComputeType {
        ec2,
        fargate
    }

    @JsonIgnore
    private ComputeType computeType = ComputeType.fargate;

    @JsonIgnore
    private boolean useSpot = false;
    private String computeEnvironmentName;

    @JsonIgnore
    private Map<String, String> tags = new LinkedHashMap<>();

    public ComputeResource() {
    }

    public ComputeType computeType() {
        return computeType;
    }

    public boolean useSpot() {
        return useSpot;
    }

    public String computeEnvironmentName() {
        return computeEnvironmentName;
    }

    public Map<String, String> tags() {
        return tags;
    }
}
