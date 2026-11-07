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
import clusterless.commons.substrate.aws.cdk.naming.ResourceNames;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.ArnComponents;
import software.amazon.awscdk.CfnTag;
import software.amazon.awscdk.CustomResource;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.iam.PolicyStatement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class EC2InstanceResourceConstruct extends ResourceConstruct<EC2InstanceResource> {
    /**
     * A persistent Spot request expires 7 days after launch unless given an end date, after which
     * a stopped instance can no longer be started. A literal, so the template, and with it the
     * launch template version the instance pins, is the same on every deploy.
     */
    static final String SPOT_VALID_UNTIL = "2099-12-31T23:59:59Z";

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
                // null selects the first subnet, an unmatched zone is reported by cdk as a synth error
                .availabilityZone(model().availabilityZone())
                .associatePublicIpAddress(true)
                .allowAllOutbound(true) // the created security group has no ingress rules
                .ssmSessionPermissions(true)
                // a shutdown from the host stops it, keeping its volume, so it can sleep without aws credentials
                .instanceInitiatedShutdownBehavior(InstanceInitiatedShutdownBehavior.STOP)
                .build());

        launchTemplate();

        if (model().useSpot()) {
            cancelSpotRequestOnDelete();
        }

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

        // cdk takes an empty zone for none, placing the instance in the first subnet's zone
        if (model.availabilityZone() != null && model.availabilityZone().isBlank()) {
            throw new IllegalArgumentException("resource: " + model.name() + ", availabilityZone may be omitted but not blank");
        }

        return model;
    }

    /**
     * In place of the instance's requireImdsv2, whose launch template is named for the construct
     * path alone, which every project and stage shares, so two instances of one name in a region
     * would collide on it.
     * <p>
     * A Spot instance takes its market options and its instance type from here. EC2 cannot change
     * the type of a Spot instance, where cloudformation changes an instance's own type in place;
     * a change to the launch template replaces the instance instead.
     * <p>
     * The name keeps its imdsv2 suffix, renaming it would replace every deployed instance.
     */
    private void launchTemplate() {
        CfnLaunchTemplate.LaunchTemplateDataProperty.Builder data = CfnLaunchTemplate.LaunchTemplateDataProperty.builder()
                .metadataOptions(CfnLaunchTemplate.MetadataOptionsProperty.builder()
                        .httpTokens("required")
                        .build());

        CfnInstance cfnInstance = (CfnInstance) instance.getInstance();

        if (model().useSpot()) {
            data.instanceType(model().instanceType())
                    .instanceMarketOptions(CfnLaunchTemplate.InstanceMarketOptionsProperty.builder()
                            .marketType("spot")
                            .spotOptions(CfnLaunchTemplate.SpotOptionsProperty.builder()
                                    // only a persistent request may be stopped, by EC2 or from within
                                    .spotInstanceType("persistent")
                                    .instanceInterruptionBehavior("stop")
                                    .validUntil(SPOT_VALID_UNTIL)
                                    .build())
                            .build())
                    // so a request can be found once its instance is gone, see SpotRequestCanceller
                    .tagSpecifications(List.of(CfnLaunchTemplate.TagSpecificationProperty.builder()
                            .resourceType("spot-instances-request")
                            .tags(spotRequestTags())
                            .build()));

            cfnInstance.addPropertyDeletionOverride("InstanceType");
        }

        CfnLaunchTemplate launchTemplate = CfnLaunchTemplate.Builder.create(this, id(model().instanceName() + "-launch-template"))
                .launchTemplateName(ResourceNames.regionUniqueScopedName(this, model().instanceName() + "-imdsv2"))
                .launchTemplateData(data.build())
                .build();

        cfnInstance.setLaunchTemplate(CfnInstance.LaunchTemplateSpecificationProperty.builder()
                .launchTemplateName(launchTemplate.getLaunchTemplateName())
                .version(launchTemplate.getAttrLatestVersionNumber()) // also orders the template first
                .build());
    }

    /**
     * Terminating an instance launched by a persistent Spot request reopens the request, which then
     * launches another instance outside the stack. The stack does not own the request, so this
     * resource cancels it, and depends on the instance so a delete cancels before the instance is
     * terminated.
     * <p>
     * Its physical id is the instance id, so a replacement instance yields a new one, and
     * cloudformation deletes the old, cancelling the replaced instance's request. Should the old
     * instance be terminated first, the delete also terminates what its reopened request launched.
     * <p>
     * The instance depends on the provider, so a provider that fails to deploy fails before the
     * instance launches, not after, when the rollback would terminate it with no one to cancel its
     * request.
     */
    private void cancelSpotRequestOnDelete() {
        SpotRequestCanceller canceller = SpotRequestCanceller.of(this);

        instance.getNode().addDependency(canceller);

        constructWithinHandler(() -> CustomResource.Builder.create(this, id(model().instanceName() + "-spot-request"))
                .resourceType(SpotRequestCanceller.RESOURCE_TYPE)
                .serviceToken(canceller.serviceToken())
                .properties(Map.of(
                        "InstanceId", instance.getInstanceId(),
                        "RequestTagKey", SpotRequestCanceller.REQUEST_TAG_KEY,
                        "RequestTagValue", spotRequestTagValue()))
                .build());
    }

    private List<CfnTag> spotRequestTags() {
        Map<String, String> tags = new LinkedHashMap<>();

        tags.put("Name", model().instanceName());
        tags.putAll(model().tags());
        tags.put(SpotRequestCanceller.REQUEST_TAG_KEY, spotRequestTagValue());

        return tags.entrySet().stream()
                .map(e -> CfnTag.builder().key(e.getKey()).value(e.getValue()).build())
                .toList();
    }

    /**
     * Unique in the region for the stage, project, version and instance, so another stack's
     * requests never match.
     */
    private String spotRequestTagValue() {
        return ResourceNames.regionUniqueScopedName(this, model().instanceName());
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
