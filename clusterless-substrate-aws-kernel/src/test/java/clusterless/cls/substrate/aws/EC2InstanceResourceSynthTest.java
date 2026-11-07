/*
 * Copyright (c) 2023-2026 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws;

import clusterless.cls.substrate.aws.resource.ec2.EC2InstanceResource;
import clusterless.cls.substrate.aws.resources.Assets;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awscdk.assertions.Match;
import software.amazon.awscdk.assertions.Template;
import software.amazon.awscdk.cxapi.CloudAssembly;
import software.amazon.awscdk.cxapi.CloudFormationStackArtifact;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Synthesizes aws:core:ec2Instance and checks the properties a host reachable only through
 * outbound connections depends on.
 */
@ExtendWith(SystemStubsExtension.class)
public class EC2InstanceResourceSynthTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(Assets.CLS_ASSETS_PATH, "build"); // we only need to point to a dir, we don't need the asset

    @TempDir
    Path configHome;

    @TempDir
    Path projects;

    @Test
    void instance() throws IOException {
        Template template = synth("""
                {
                  "type": "aws:core:ec2Instance",
                  "name": "lab",
                  "instanceName": "lab-0",
                  "instanceType": "m7i.4xlarge",
                  "rootVolumeSize": 200,
                  "userData": ["echo first-boot"],
                  "readableParameters": ["/lab/tailscale-key"],
                  "tags": {"role": "lab"}
                }
                """);

        template.resourceCountIs("AWS::EC2::Instance", 1);
        template.hasResourceProperties("AWS::EC2::Instance", Match.objectLike(Map.of(
                "InstanceType", "m7i.4xlarge",
                "InstanceInitiatedShutdownBehavior", "stop",
                "NetworkInterfaces", List.of(Match.objectLike(Map.of("AssociatePublicIpAddress", true))),
                "BlockDeviceMappings", List.of(Map.of(
                        "DeviceName", "/dev/sda1",
                        "Ebs", Match.objectLike(Map.of("VolumeSize", 200, "VolumeType", "gp3", "Encrypted", true, "DeleteOnTermination", true)))),
                "UserData", Map.of("Fn::Base64", Match.stringLikeRegexp("echo first-boot")),
                "Tags", Match.arrayWith(List.of(Map.of("Key", "Name", "Value", "lab-0"), Map.of("Key", "role", "Value", "lab")))
        )));

        // from the ssm parameter, resolved by cloudformation at deploy
        template.hasParameter("*", Match.objectLike(Map.of("Default", EC2InstanceResource.UBUNTU_LTS_AMD64)));

        // named for the stage, project and version, not the construct path every project shares
        // on demand, the launch template carries only imdsv2, as before spot was supported,
        // so deployed instances are not replaced
        template.resourceCountIs("AWS::EC2::LaunchTemplate", 1);
        template.hasResourceProperties("AWS::EC2::LaunchTemplate", Match.objectLike(Map.of(
                "LaunchTemplateName", "TEST-lab-lab0-imdsv2-20261023-00",
                "LaunchTemplateData", Match.exact(Map.of("MetadataOptions", Map.of("HttpTokens", "required"))))));
        template.resourceCountIs("Custom::SpotInstanceRequest", 0);
        template.hasResourceProperties("AWS::EC2::Instance", Match.objectLike(Map.of(
                "LaunchTemplate", Match.objectLike(Map.of("LaunchTemplateName", "TEST-lab-lab0-imdsv2-20261023-00")))));

        template.hasResourceProperties("AWS::EC2::SecurityGroup", Match.objectLike(Map.of("SecurityGroupIngress", Match.absent())));
        template.resourceCountIs("AWS::EC2::SecurityGroupIngress", 0);

        // {"Fn::Join": ["", ["arn:", {"Ref": "AWS::Partition"}, ":iam::aws:policy/AmazonSSMManagedInstanceCore"]]}
        Object sessionManagerArn = Match.objectLike(Map.of(
                "Fn::Join", Match.arrayWith(List.of(Match.arrayWith(List.of(":iam::aws:policy/AmazonSSMManagedInstanceCore"))))));

        template.hasResourceProperties("AWS::IAM::Role", Match.objectLike(Map.of(
                "ManagedPolicyArns", Match.arrayWith(List.of(sessionManagerArn)))));

        // the leading slash of the parameter name is not repeated after "parameter"
        Object readParameter = Match.objectLike(Map.of(
                "Action", List.of("ssm:GetParameter", "ssm:GetParameters"),
                "Resource", Match.objectLike(Map.of(
                        "Fn::Join", Match.arrayWith(List.of(Match.arrayWith(List.of(":ssm:us-west-2:000000000000:parameter/lab/tailscale-key"))))))));

        template.hasResourceProperties("AWS::IAM::Policy", Match.objectLike(Map.of(
                "PolicyDocument", Match.objectLike(Map.of("Statement", Match.arrayWith(List.of(readParameter)))))));

        Assertions.assertEquals(List.of("ref:aws:id:test:lab:20261023-00:core:ec2-instance:lab"), exportNames(template));
    }

    @Test
    void spot() throws IOException {
        String resource = """
                {
                  "type": "aws:core:ec2Instance",
                  "name": "lab",
                  "instanceName": "lab-0",
                  "instanceType": "m7i.4xlarge",
                  "useSpot": true,
                  "tags": {"role": "lab"}
                }
                """;

        Template template = synth(resource);

        // the type moves to the launch template, so a change replaces the instance, EC2 cannot change a spot instance's type
        template.hasResourceProperties("AWS::EC2::LaunchTemplate", Match.objectLike(Map.of(
                "LaunchTemplateName", "TEST-lab-lab0-imdsv2-20261023-00",
                "LaunchTemplateData", Match.exact(Map.of(
                        "InstanceType", "m7i.4xlarge",
                        "MetadataOptions", Map.of("HttpTokens", "required"),
                        "InstanceMarketOptions", Map.of(
                                "MarketType", "spot",
                                "SpotOptions", Map.of(
                                        "SpotInstanceType", "persistent",
                                        "InstanceInterruptionBehavior", "stop",
                                        "ValidUntil", "2099-12-31T23:59:59Z")),
                        // the request can be found by tag once its instance is gone
                        "TagSpecifications", List.of(Map.of(
                                "ResourceType", "spot-instances-request",
                                "Tags", List.of(
                                        Map.of("Key", "Name", "Value", "lab-0"),
                                        Map.of("Key", "role", "Value", "lab"),
                                        Map.of("Key", "cls:ec2-instance", "Value", "TEST-lab-lab0-20261023-00")))))))));

        template.hasResourceProperties("AWS::EC2::Instance", Match.objectLike(Map.of(
                "InstanceType", Match.absent(),
                "InstanceInitiatedShutdownBehavior", "stop")));

        // cancels the request on delete, which depends on the instance through its id
        String instanceId = template.findResources("AWS::EC2::Instance").keySet().iterator().next();

        template.resourceCountIs("Custom::SpotInstanceRequest", 1);
        template.hasResourceProperties("Custom::SpotInstanceRequest", Match.objectLike(Map.of(
                "InstanceId", Map.of("Ref", instanceId),
                "RequestTagKey", "cls:ec2-instance",
                "RequestTagValue", "TEST-lab-lab0-20261023-00")));

        // the provider deploys before the instance launches, and is deleted after it terminates
        String handlerId = template.findResources("AWS::Lambda::Function", Match.objectLike(Map.of(
                "Properties", Match.objectLike(Map.of("Code", Map.of("ZipFile", Match.stringLikeRegexp("CancelSpotInstanceRequestsCommand")))))))
                .keySet().iterator().next();
        template.hasResource("AWS::EC2::Instance", Match.objectLike(Map.of(
                "DependsOn", Match.arrayWith(List.of(handlerId)))));

        // the handler, inline, and the calls its role may make
        template.hasResourceProperties("AWS::Lambda::Function", Match.objectLike(Map.of(
                "Code", Map.of("ZipFile", Match.stringLikeRegexp("CancelSpotInstanceRequestsCommand")),
                "MemorySize", 256)));

        Object describe = Match.objectLike(Map.of("Action", List.of("ec2:DescribeInstances", "ec2:DescribeSpotInstanceRequests"), "Resource", "*"));
        Object cancel = Match.objectLike(Map.of("Action", "ec2:CancelSpotInstanceRequests", "Resource", arn("spot-instances-request")));
        Object terminate = Match.objectLike(Map.of("Action", "ec2:TerminateInstances", "Resource", arn("instance")));
        Object stackStatus = Match.objectLike(Map.of("Action", "cloudformation:DescribeStacks", "Resource", Map.of("Ref", "AWS::StackId")));

        template.hasResourceProperties("AWS::IAM::Policy", Match.objectLike(Map.of(
                "PolicyDocument", Match.objectLike(Map.of("Statement", Match.arrayWith(List.of(describe, cancel, terminate, stackStatus)))))));
    }

    @Test
    void spotProviderPerStack() throws IOException {
        Path project = projects.resolve("project.json");

        Files.writeString(project, """
                {
                  "project": {"name": "Lab", "version": "20261023-00"},
                  "placement": {"stage": "test", "provider": "aws", "account": "000000000000", "region": "us-west-2"},
                  "resources": [
                    {"type": "aws:core:ec2Instance", "name": "lab0", "instanceName": "lab-0", "instanceType": "m7i.4xlarge", "useSpot": true},
                    {"type": "aws:core:ec2Instance", "name": "lab1", "instanceName": "lab-1", "instanceType": "m7i.4xlarge", "useSpot": true}
                  ]
                }
                """);

        Template template = template(project);

        template.resourceCountIs("Custom::SpotInstanceRequest", 2);
        template.resourcePropertiesCountIs("AWS::Lambda::Function", Match.objectLike(Map.of(
                "Code", Map.of("ZipFile", Match.stringLikeRegexp("CancelSpotInstanceRequestsCommand")))), 1);
    }

    @Test
    void rejectsBlankAvailabilityZone() {
        Throwable thrown = Assertions.assertThrows(RuntimeException.class, () -> synth("""
                {
                  "type": "aws:core:ec2Instance",
                  "name": "lab",
                  "instanceName": "lab-0",
                  "instanceType": "m7i.4xlarge",
                  "availabilityZone": ""
                }
                """));

        Assertions.assertTrue(messages(thrown).contains("availabilityZone may be omitted but not blank"), messages(thrown));
    }

    @Test
    void availabilityZone() throws IOException {
        // the vpc lookup has no context here, so cdk provides its dummy vpc and its zones
        Template template = synth("""
                {
                  "type": "aws:core:ec2Instance",
                  "name": "lab",
                  "instanceName": "lab-0",
                  "instanceType": "m7i.4xlarge",
                  "availabilityZone": "dummy1b"
                }
                """);

        template.hasResourceProperties("AWS::EC2::Instance", Match.objectLike(Map.of("AvailabilityZone", "dummy1b")));
    }

    @Test
    void pinnedImage() throws IOException {
        Template template = synth("""
                {
                  "type": "aws:core:ec2Instance",
                  "name": "lab",
                  "instanceName": "lab-0",
                  "instanceType": "m7i.4xlarge",
                  "machineImage": "ami-0123456789abcdef0"
                }
                """);

        template.hasResourceProperties("AWS::EC2::Instance", Match.objectLike(Map.of("ImageId", "ami-0123456789abcdef0")));
    }

    @Test
    void requiresInstanceType() {
        Throwable thrown = Assertions.assertThrows(RuntimeException.class, () -> synth("""
                {
                  "type": "aws:core:ec2Instance",
                  "name": "lab",
                  "instanceName": "lab-0"
                }
                """));

        Assertions.assertTrue(messages(thrown).contains("requires an instanceType"), messages(thrown));
    }

    @Test
    void rejectsUnknownImageForm() {
        Throwable thrown = Assertions.assertThrows(RuntimeException.class, () -> synth("""
                {
                  "type": "aws:core:ec2Instance",
                  "name": "lab",
                  "instanceName": "lab-0",
                  "instanceType": "m7i.4xlarge",
                  "machineImage": "ubuntu-24.04"
                }
                """));

        Assertions.assertTrue(messages(thrown).contains("machineImage must be"), messages(thrown));
    }

    private Template synth(String resource) throws IOException {
        Path project = projects.resolve("project.json");

        Files.writeString(project, """
                {
                  "project": {"name": "Lab", "version": "20261023-00"},
                  "placement": {"stage": "test", "provider": "aws", "account": "000000000000", "region": "us-west-2"},
                  "resources": [%s]
                }
                """.formatted(resource));

        return template(project);
    }

    private Template template(Path project) throws IOException {
        CloudAssembly assembly = SynthSupport.synthProject(project.toFile(), configHome);

        List<CloudFormationStackArtifact> stacks = assembly.getStacks();
        Assertions.assertEquals(1, stacks.size(), "stacks");

        return Template.fromJSON(MAPPER.convertValue(stacks.get(0).getTemplate(), Map.class));
    }

    @SuppressWarnings("unchecked")
    private static List<String> exportNames(Template template) {
        Map<String, Object> outputs = (Map<String, Object>) template.toJSON().getOrDefault("Outputs", Map.of());

        return outputs.values().stream()
                .map(output -> ((Map<String, Object>) output).get("Export"))
                .filter(Map.class::isInstance)
                .map(export -> String.valueOf(((Map<?, ?>) export).get("Name")))
                .toList();
    }

    // {"Fn::Join": ["", ["arn:", {"Ref": "AWS::Partition"}, ":ec2:us-west-2:000000000000:<resource>/*"]]}
    private static Object arn(String resource) {
        return Match.objectLike(Map.of(
                "Fn::Join", Match.arrayWith(List.of(Match.arrayWith(List.of(":ec2:us-west-2:000000000000:" + resource + "/*"))))));
    }

    private static String messages(Throwable thrown) {
        StringBuilder builder = new StringBuilder();

        for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
            builder.append(cause.getMessage()).append('\n');
        }

        return builder.toString();
    }
}
