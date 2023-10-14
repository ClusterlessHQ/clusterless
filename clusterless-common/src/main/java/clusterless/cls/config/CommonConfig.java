/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.config;

import java.nio.file.Paths;

/**
 *
 */
public class CommonConfig extends Configuration {
    public static final ConfigOptions configOptions = ConfigOptions.Builder.builder()
            .withLocalConfigName(Paths.get(ConfigManager.LOCAL_CONFIG_NAME))
            .withGlobalConfigName(Paths.get(ConfigManager.GLOBAL_CONFIG_NAME))
            .withConfigNamespace("common")
            .withConfigClass(CommonConfig.class)
            .build();

    ResourceConfig resource = new ResourceConfig();

    public CommonConfig() {
    }

    @Override
    public String name() {
        return "common";
    }

    public ResourceConfig resource() {
        return resource;
    }
}
