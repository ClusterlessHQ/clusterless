/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.config.ConfigOptions;
import clusterless.config.Configuration;

import java.nio.file.Paths;

/**
 * --require-approval     What security-sensitive changes need manual
 * approval
 * [string] [choices: "never", "any-change", "broadening"]
 */
public class AwsConfig extends Configuration {
    public static final ConfigOptions configOptions = ConfigOptions.Builder.builder()
            .withLocalConfigName(Paths.get(".clsconfig-aws"))
            .withGlobalConfigName(Paths.get("config-aws"))
            .withConfigClass(AwsConfig.class)
            .build();

    public AwsConfig() {
    }

    @Override
    public String name() {
        return "aws";
    }
}
