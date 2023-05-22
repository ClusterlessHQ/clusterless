/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;

import clusterless.substrate.aws.CommonCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class CDKCommand extends CommonCommand {

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