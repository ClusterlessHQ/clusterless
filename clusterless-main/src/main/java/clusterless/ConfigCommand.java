/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless;

import clusterless.config.CommonConfig;
import clusterless.config.ConfigManager;
import clusterless.config.Configuration;
import clusterless.substrate.SubstrateProvider;
import picocli.CommandLine;

import java.lang.reflect.InvocationTargetException;

/**
 * A command to manage config files
 */
@CommandLine.Command(
        name = "config",
        description = "Manage local and global configuration settings."
)
public class ConfigCommand {
    @CommandLine.ParentCommand
    Main main;

    @CommandLine.Command(
            name = "show",
            description = "Print out current visible configuration values."
    )
    public Integer show(
            @CommandLine.Option(
                    names = "--config",
                    description = "Provider name to show.",
                    defaultValue = "common"
            ) String name
    ) {
        main.printer().println(ConfigManager.toString(loadConfigurationFor(name)));

        return 0;
    }

    @CommandLine.Command(
            name = "init",
            description = {
                    "Create a local or global configuration file.",
                    "Set a provider name via --config to initialize a provider specific configuration."
            }
    )
    public Integer init(
            @CommandLine.Option(
                    names = "--config",
                    description = "The provider name to initialize.",
                    defaultValue = "common"
            ) String name,
            @CommandLine.Option(
                    names = "--global",
                    description = "Write a default global configuration file."
            ) boolean global,
            @CommandLine.Option(
                    names = "--merge",
                    description = {
                            "Merge all visible configuration files and cli options before writing.",
                            "Use this option to capture global and local configuration settings into a single configuration file."
                    }
            ) boolean merge,
            @CommandLine.Option(
                    names = "--force",
                    description = "Overwrite any existing configuration file."
            ) boolean force
    ) {
        Configuration configuration = merge ? loadConfigurationFor(name) : newConfigFor(name);

        if (global) {
            ConfigManager.writeGlobalConfig(ConfigManager.optionsFor(name, configuration.getClass()), configuration, force);
        } else {
            ConfigManager.writeLocalConfig(ConfigManager.optionsFor(name, configuration.getClass()), configuration, force);
        }

        return CommandLine.ExitCode.OK;
    }

    private Configuration loadConfigurationFor(String name) {
        Configuration configuration = ConfigManager.loadConfig(ConfigManager.optionsFor(name, findConfigClassFor(name)));

        if (configuration == null) {
            throw new IllegalStateException("no configuration found for: " + name);
        }

        return configuration;
    }

    private Configuration newConfigFor(String name) {
        try {
            return findConfigClassFor(name).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<? extends Configuration> findConfigClassFor(String name) {
        Class<? extends Configuration> configClass;

        if (name.equals("common")) {
            configClass = CommonConfig.class;
        } else {
            SubstrateProvider substrateProvider = main.substratesOptions().requestedProvider().get(name);
            configClass = substrateProvider.configClass();
        }

        return configClass;
    }
}
