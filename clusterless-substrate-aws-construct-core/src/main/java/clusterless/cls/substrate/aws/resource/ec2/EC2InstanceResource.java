/*
 * Copyright (c) 2023-2026 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.ec2;

import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.model.deploy.Resource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates and maintains a single EC2 instance in the bootstrap VPC.
 */
public class EC2InstanceResource extends Resource {
    public static final String UBUNTU_LTS_AMD64 = "/aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id";

    @JsonRequiredProperty
    private String instanceName;
    @JsonRequiredProperty
    private String instanceType;
    /**
     * Either an SSM parameter name holding an AMI id (begins with {@code /}), resolved on every deploy,
     * or an AMI id (begins with {@code ami-}).
     */
    private String machineImage = UBUNTU_LTS_AMD64;
    private String rootDeviceName = "/dev/sda1";
    private int rootVolumeSize = 100;
    private List<String> userData = new ArrayList<>();
    private List<String> readableParameters = new ArrayList<>();
    private Map<String, String> tags = new LinkedHashMap<>();

    public EC2InstanceResource() {
    }

    public String instanceName() {
        return instanceName;
    }

    public String instanceType() {
        return instanceType;
    }

    public String machineImage() {
        return machineImage;
    }

    public String rootDeviceName() {
        return rootDeviceName;
    }

    public int rootVolumeSize() {
        return rootVolumeSize;
    }

    public List<String> userData() {
        return userData;
    }

    public List<String> readableParameters() {
        return readableParameters;
    }

    public Map<String, String> tags() {
        return tags;
    }
}
