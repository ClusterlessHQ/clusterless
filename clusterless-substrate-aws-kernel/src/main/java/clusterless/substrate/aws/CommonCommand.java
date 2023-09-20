/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.config.CommonConfig;
import clusterless.substrate.aws.cdk.Provider;
import clusterless.util.Strings;
import picocli.CommandLine;

public class CommonCommand {
    @CommandLine.ParentCommand
    protected Kernel kernel;

    protected CommonConfig getCommonConfig() {
        return kernel.configurations().get("common");
    }

    protected AwsConfig getProviderConfig() {
        return kernel.configurations().get(Provider.NAME);
    }

    protected int verbosityLevel() {
        return kernel.verbosity().level();
    }

    protected String prompt(String value, String prompt) {
        if (value == null && System.console() != null) {
            return Strings.emptyToNull(System.console().readLine(prompt));
        }

        return value;
    }
}
