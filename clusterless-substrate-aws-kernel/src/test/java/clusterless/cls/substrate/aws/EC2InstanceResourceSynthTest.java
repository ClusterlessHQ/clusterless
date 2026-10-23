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

        template.hasResourceProperties("AWS::EC2::LaunchTemplate", Match.objectLike(Map.of(
                "LaunchTemplateData", Match.objectLike(Map.of("MetadataOptions", Map.of("HttpTokens", "required"))))));

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

    private static String messages(Throwable thrown) {
        StringBuilder builder = new StringBuilder();

        for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
            builder.append(cause.getMessage()).append('\n');
        }

        return builder.toString();
    }
}
