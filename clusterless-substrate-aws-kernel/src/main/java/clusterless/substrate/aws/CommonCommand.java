/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.config.CommonConfig;
import clusterless.substrate.aws.cdk.CDK;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class CommonCommand {
    @CommandLine.ParentCommand
    protected Kernel kernel;

    protected CommonConfig getCommonConfig() {
        return kernel.configurations().get("common");
    }

    protected AwsConfig getProviderConfig() {
        return kernel.configurations().get(CDK.PROVIDER);
    }

    @NotNull
    protected List<String> getRequireDeployApproval() {
        return List.of(
                "--require-approval", getProviderConfig().cdk().requireDeployApproval().value()
        );
    }

    @NotNull
    protected List<String> getRequireDestroyApproval() {
        if (!getProviderConfig().cdk().requireDestroyApproval()) {
            return List.of(
                    "--force"
            );
        }

        return Collections.emptyList();
    }
}
