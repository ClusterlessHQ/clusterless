/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.config;

import clusterless.cls.util.Lazy;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Supplier;

/**
 *
 */
public class Configurations {
    private final Supplier<Properties> properties;
    final Map<String, Lazy<Configuration>> configMap = new LinkedHashMap<>();

    public Configurations(Supplier<Properties> properties) {
        this.properties = properties;
    }

    public void add(ConfigOptions configOptions) {
        configMap.put(configOptions.configNamespace(), Lazy.of(() -> ConfigManager.loadConfig(properties.get(), configOptions)));
    }

    public <C extends Config> C get(String name) {
        if (configMap.containsKey(name)) {
            return (C) configMap.get(name).get();
        }

        throw new IllegalStateException("config not found: " + name);
    }
}
