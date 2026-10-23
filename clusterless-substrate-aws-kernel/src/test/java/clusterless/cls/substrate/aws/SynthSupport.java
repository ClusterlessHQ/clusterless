/*
 * Copyright (c) 2023-2026 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
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
import clusterless.cls.substrate.aws.cdk.lifecycle.Lifecycle;
import clusterless.cls.substrate.aws.managed.ManagedApp;
import software.amazon.awscdk.cxapi.CloudAssembly;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Synthesizes a project file in the current JVM, the way the Kernel does, without the CLI.
 */
public class SynthSupport {
    /**
     * @param configHome an empty directory, so a developer's ~/.cls or .clsconfig cannot change the synthesized output
     */
    public static CloudAssembly synthProject(File project, Path configHome) throws IOException {
        Lifecycle lifecycle = new Lifecycle();
        lifecycle.setConfigurations(isolatedConfigurations(configHome));

        List<Deployable> deployables = lifecycle.loadProjectModels(List.of(project));
        ManagedApp managedApp = lifecycle.mapProject(false, deployables, Collections.emptyList());

        return managedApp.synth();
    }

    /**
     * Mirrors the namespaces the Kernel registers, but reads config files from configHome.
     */
    private static Configurations isolatedConfigurations(Path configHome) {
        Configurations configurations = new Configurations(Properties::new);

        configurations.add(isolated(configHome, "common", ConfigManager.LOCAL_CONFIG_NAME, ConfigManager.GLOBAL_CONFIG_NAME, CommonConfig.class));
        configurations.add(isolated(configHome, "aws", ConfigManager.LOCAL_CONFIG_NAME + "-aws", ConfigManager.GLOBAL_CONFIG_NAME + "-aws", AwsConfig.class));

        return configurations;
    }

    private static ConfigOptions isolated(Path configHome, String namespace, String localName, String globalName, Class<? extends Configuration> configClass) {
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
}
