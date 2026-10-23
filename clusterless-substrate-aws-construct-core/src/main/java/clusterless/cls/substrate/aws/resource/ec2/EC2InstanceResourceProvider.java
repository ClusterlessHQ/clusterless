/*
 * Copyright (c) 2023-2026 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.ec2;

import clusterless.cls.managed.component.ProvidesComponent;
import clusterless.cls.managed.component.ResourceComponentService;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:ec2Instance",
        synopsis = "Create an AWS EC2 instance.",
        description = """
                A single EC2 instance in the bootstrap VPC, for work that needs a long-lived host.

                The instance will be configured with the following:
                    a public ip address, so it can reach the internet from the bootstrap subnets
                    a security group allowing all outbound traffic and no inbound traffic
                    IMDSv2 required
                    SSM Session Manager permissions, for shell access without inbound rules
                    an encrypted gp3 root volume, deleted with the instance
                    a shutdown from within the instance stops it, rather than terminating it

                The instance is terminated, and its root volume deleted, when the project is destroyed.
                Stopping and starting it outside of a deploy does not change the stack.

                instanceName: a name for the instance
                    Applied as the instance Name tag.

                instanceType: an EC2 instance type, e.g. m7i.4xlarge

                machineImage: an SSM parameter name or an AMI id
                    An SSM parameter name begins with '/' and is resolved on every deploy, so a
                    deploy after the image is updated replaces the instance. An AMI id begins
                    with 'ami-' and pins the image.
                    Defaults to the current Ubuntu 24.04 LTS amd64 image.

                rootDeviceName: the root device name of the machine image
                    Defaults to /dev/sda1 (Ubuntu); Amazon Linux uses /dev/xvda.

                rootVolumeSize: the root volume size in GiB
                    Defaults to 100.

                userData: [ "command", ... ]
                    Shell commands run as root on first boot.

                readableParameters: [ "/parameter/name", ... ]
                    SSM parameter names the instance may read, including SecureString parameters
                    encrypted with the AWS managed key. Keep secrets here, not in userData, which
                    is stored in plain text in the stack template.

                tags: { key: value, ... }
                    Tags to apply to the instance.
                """
)
public class EC2InstanceResourceProvider implements ResourceComponentService<ManagedComponentContext, EC2InstanceResource, EC2InstanceResourceConstruct> {

    @Override
    public EC2InstanceResourceConstruct create(ManagedComponentContext context, EC2InstanceResource model) {
        return new EC2InstanceResourceConstruct(context, model);
    }

    @Override
    public Class<EC2InstanceResource> modelClass() {
        return EC2InstanceResource.class;
    }
}
