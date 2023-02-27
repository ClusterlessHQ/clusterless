/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class Configurations {
    final Map<String, Configuration> configMap = new LinkedHashMap<>();

    public Configurations() {
    }

    public void add(ConfigOptions configOptions) {
        Configuration config = ConfigManager.loadConfig(configOptions);

        configMap.put(config.name(), config);
    }

    public <C extends Config> C get(String name) {
        if (configMap.containsKey(name)) {
            return (C) configMap.get(name);
        }

        throw new IllegalStateException("config not found: " + name);
    }
}
