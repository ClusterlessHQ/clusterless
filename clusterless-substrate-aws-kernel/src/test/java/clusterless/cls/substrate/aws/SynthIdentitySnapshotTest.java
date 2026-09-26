/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws;

import clusterless.cls.config.CommonConfig;
import clusterless.cls.config.ConfigManager;
import clusterless.cls.config.ConfigOptions;
import clusterless.cls.config.Configuration;
import clusterless.cls.config.Configurations;
import clusterless.cls.model.deploy.Deployable;
import clusterless.cls.substrate.aws.cdk.bootstrap.BootstrapApp;
import clusterless.cls.substrate.aws.cdk.bootstrap.BootstrapStack;
import clusterless.cls.substrate.aws.cdk.lifecycle.Lifecycle;
import clusterless.cls.substrate.aws.managed.ManagedApp;
import clusterless.cls.substrate.aws.resources.Assets;
import clusterless.cls.substrate.aws.resources.Stacks;
import clusterless.commons.naming.Stage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.cxapi.CloudAssembly;
import software.amazon.awscdk.cxapi.CloudFormationStackArtifact;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Pins the deployed identity of the synthesized stacks: stack names, CloudFormation
 * logical ids and their resource types, physical names, lambda handlers and props
 * environment keys, and export names. A change to any of these replaces or orphans
 * deployed resources, so a diff here must be deliberate.
 * <p>
 * The projects under {@code src/test/resources/snapshot/projects} are the
 * {@code clusterless-scenario} scenarios rendered with jsonnet using stage {@code test},
 * account {@code 000000000000}, and region {@code us-west-2}.
 * <p>
 * To accept a deliberate change, regenerate the snapshot and review its diff:
 * {@code CLS_SNAPSHOT_UPDATE=true ./gradlew :clusterless-substrate-aws-kernel:test --tests '*SynthIdentitySnapshotTest'}
 */
@ExtendWith(SystemStubsExtension.class)
public class SynthIdentitySnapshotTest {
    private static final Path PROJECTS = Paths.get("src/test/resources/snapshot/projects");
    private static final Path SNAPSHOT = Paths.get("src/test/resources/snapshot/synth-identity.json");
    private static final boolean UPDATE = Boolean.parseBoolean(System.getenv("CLS_SNAPSHOT_UPDATE"));

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .enable(SerializationFeature.INDENT_OUTPUT);

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(Assets.CLS_ASSETS_PATH, "build"); // we only need to point to a dir, we don't need the asset

    @TempDir
    Path configHome;

    @Test
    void synthesizedIdentityMatchesSnapshot() throws IOException {
        Map<String, Object> actual = new TreeMap<>();

        List<Path> projects;
        try (Stream<Path> stream = Files.list(PROJECTS)) {
            projects = stream.filter(p -> p.toString().endsWith(".json")).sorted().toList();
        }

        Assertions.assertFalse(projects.isEmpty(), "no snapshot projects found in: " + PROJECTS.toAbsolutePath());

        for (Path project : projects) {
            actual.put(project.getFileName().toString(), identityOf(synthProject(project.toFile())));
        }

        actual.put("bootstrap", identityOf(synthBootstrap()));

        String rendered = MAPPER.writeValueAsString(actual) + "\n";

        if (UPDATE) {
            Files.writeString(SNAPSHOT, rendered);
            return;
        }

        Assertions.assertTrue(Files.exists(SNAPSHOT), "no snapshot, generate it with CLS_SNAPSHOT_UPDATE=true: " + SNAPSHOT.toAbsolutePath());
        Assertions.assertEquals(Files.readString(SNAPSHOT), rendered,
                "synthesized deployed identity changed; if deliberate, regenerate with CLS_SNAPSHOT_UPDATE=true and review the diff");
    }

    private CloudAssembly synthProject(File project) throws IOException {
        Lifecycle lifecycle = new Lifecycle();
        lifecycle.setConfigurations(isolatedConfigurations());

        List<Deployable> deployables = lifecycle.loadProjectModels(List.of(project));
        ManagedApp managedApp = lifecycle.mapProject(false, deployables, Collections.emptyList());

        return managedApp.synth();
    }

    private CloudAssembly synthBootstrap() {
        Stage stage = Stage.of("test");
        App app = new BootstrapApp(AppProps.builder().build(), stage);

        new BootstrapStack((BootstrapApp) app, StackProps.builder()
                .stackName(Stacks.bootstrapStackName(stage))
                .env(Environment.builder()
                        .account("000000000000")
                        .region("us-west-2")
                        .build())
                .build());

        return app.synth();
    }

    /**
     * Mirrors the namespaces the Kernel registers, but reads config files from an empty
     * directory so a developer's ~/.cls or .clsconfig cannot change the synthesized output.
     */
    private Configurations isolatedConfigurations() {
        Configurations configurations = new Configurations(Properties::new);

        configurations.add(isolated("common", ConfigManager.LOCAL_CONFIG_NAME, ConfigManager.GLOBAL_CONFIG_NAME, CommonConfig.class));
        configurations.add(isolated("aws", ConfigManager.LOCAL_CONFIG_NAME + "-aws", ConfigManager.GLOBAL_CONFIG_NAME + "-aws", AwsConfig.class));

        return configurations;
    }

    private ConfigOptions isolated(String namespace, String localName, String globalName, Class<? extends Configuration> configClass) {
        return ConfigOptions.Builder.builder()
                .withHomePath(configHome)
                .withLocalPath(configHome)
                .withGlobalConfigPath(configHome.resolve(".cls"))
                .withLocalConfigName(Paths.get(localName))
                .withGlobalConfigName(Paths.get(globalName))
                .withConfigNamespace(namespace)
                .withConfigClass(configClass)
                .build();
    }

    private static Map<String, Object> identityOf(CloudAssembly assembly) {
        Map<String, Object> stacks = new TreeMap<>();

        for (CloudFormationStackArtifact stack : assembly.getStacks()) {
            Map<String, Object> template = MAPPER.convertValue(stack.getTemplate(), Map.class);
            stacks.put(stack.getStackName(), stackIdentity(template));
        }

        return stacks;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> stackIdentity(Map<String, Object> template) {
        Map<String, Object> resources = new TreeMap<>();

        Map<String, Object> templateResources = (Map<String, Object>) template.getOrDefault("Resources", Map.of());

        for (Map.Entry<String, Object> entry : templateResources.entrySet()) {
            Map<String, Object> resource = (Map<String, Object>) entry.getValue();
            String type = (String) resource.get("Type");

            if ("AWS::CDK::Metadata".equals(type)) {
                continue;
            }

            Map<String, Object> properties = (Map<String, Object>) resource.getOrDefault("Properties", Map.of());
            Map<String, Object> identity = new TreeMap<>();

            identity.put("Type", type);

            for (Map.Entry<String, Object> property : properties.entrySet()) {
                String key = property.getKey();
                Object value = property.getValue();

                // physical names, and names of the resources this one binds to
                if (key.endsWith("Name") || key.equals("Handler")) {
                    identity.put(key, value);
                }

                // glue database and table names are nested in their input structures
                if (key.endsWith("Input") && value instanceof Map<?, ?> input && input.containsKey("Name")) {
                    identity.put(key + ".Name", input.get("Name"));
                }

                // lambda props contract: CLS_<SimpleClassName>_JAVA / _JSON
                if (key.equals("Environment") && value instanceof Map<?, ?> environment && environment.get("Variables") instanceof Map<?, ?> variables) {
                    identity.put("Environment.Variables", new TreeMap<>(variables).keySet());
                }
            }

            resources.put(entry.getKey(), identity);
        }

        Map<String, Object> exports = new TreeMap<>();
        Map<String, Object> outputs = (Map<String, Object>) template.getOrDefault("Outputs", Map.of());

        for (Map.Entry<String, Object> entry : outputs.entrySet()) {
            Map<String, Object> output = (Map<String, Object>) entry.getValue();

            if (output.get("Export") instanceof Map<?, ?> export) {
                exports.put(entry.getKey(), export.get("Name"));
            }
        }

        Map<String, Object> identity = new TreeMap<>();

        identity.put("Resources", resources);

        if (!exports.isEmpty()) {
            identity.put("Exports", exports);
        }

        return identity;
    }
}
