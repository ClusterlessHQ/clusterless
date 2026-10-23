/*
 * Copyright (c) 2023-2026 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.ec2;

import clusterless.cls.substrate.aws.construct.ResourceConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.resources.Vpcs;
import clusterless.cls.substrate.aws.util.TagsUtil;
import clusterless.commons.naming.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.ArnComponents;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.iam.PolicyStatement;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class EC2InstanceResourceConstruct extends ResourceConstruct<EC2InstanceResource> {
    private final Instance instance;

    public EC2InstanceResourceConstruct(@NotNull ManagedComponentContext context, @NotNull EC2InstanceResource model) {
        super(context, verify(model), Label.of(model.instanceName()));

        UserData userData = UserData.forLinux();
        model().userData().forEach(userData::addCommands);

        instance = constructWithinHandler(() -> Instance.Builder.create(this, id(model().instanceName()))
                .instanceName(model().instanceName())
                .instanceType(new InstanceType(model().instanceType()))
                .machineImage(machineImage(model().machineImage()))
                .userData(userData)
                .blockDevices(List.of(BlockDevice.builder()
                        .deviceName(model().rootDeviceName())
                        .volume(BlockDeviceVolume.ebs(model().rootVolumeSize(), EbsDeviceOptions.builder()
                                .volumeType(EbsDeviceVolumeType.GP3)
                                .encrypted(true)
                                .deleteOnTermination(true)
                                .build()))
                        .build()))
                .vpc(Vpcs.lookupVpc(this, context))
                // the bootstrap subnets are public but do not map a public ip on launch,
                // without one the instance has no route out
                .vpcSubnets(SubnetSelection.builder().subnetType(SubnetType.PUBLIC).build())
                .associatePublicIpAddress(true)
                .allowAllOutbound(true) // the created security group has no ingress rules
                .requireImdsv2(true)
                .ssmSessionPermissions(true)
                // a shutdown from the host stops it, keeping its volume, so it can sleep without aws credentials
                .instanceInitiatedShutdownBehavior(InstanceInitiatedShutdownBehavior.STOP)
                .build());

        if (!model().readableParameters().isEmpty()) {
            List<String> parameterArns = model().readableParameters().stream()
                    .map(this::parameterArn)
                    .toList();

            instance.addToRolePolicy(PolicyStatement.Builder.create()
                    .actions(List.of("ssm:GetParameter", "ssm:GetParameters"))
                    .resources(parameterArns)
                    .build());
        }

        TagsUtil.applyTags(instance, model().tags());

        exportIdRefFor(model(), instance, instance.getInstanceId(), "ec2 instance id");
    }

    private static EC2InstanceResource verify(EC2InstanceResource model) {
        if (model.instanceName() == null || model.instanceName().isBlank()) {
            throw new IllegalArgumentException("resource: " + model.name() + ", requires an instanceName");
        }

        if (model.instanceType() == null || model.instanceType().isBlank()) {
            throw new IllegalArgumentException("resource: " + model.name() + ", requires an instanceType, e.g. m7i.4xlarge");
        }

        String image = model.machineImage();

        if (image == null || !(image.startsWith("/") || image.startsWith("ami-"))) {
            throw new IllegalArgumentException("resource: " + model.name() + ", machineImage must be an SSM parameter name beginning with '/' or an AMI id beginning with 'ami-', got: " + image);
        }

        return model;
    }

    private IMachineImage machineImage(String image) {
        if (image.startsWith("ami-")) {
            return MachineImage.genericLinux(Map.of(Stack.of(this).getRegion(), image));
        }

        return MachineImage.fromSsmParameter(image);
    }

    private String parameterArn(String parameterName) {
        // hierarchical names carry a leading slash the arn does not repeat
        return Stack.of(this).formatArn(ArnComponents.builder()
                .service("ssm")
                .resource("parameter")
                .resourceName(parameterName.startsWith("/") ? parameterName.substring(1) : parameterName)
                .build());
    }

    public Instance instance() {
        return instance;
    }
}
