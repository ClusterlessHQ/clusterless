/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import picocli.CommandLine;

@CommandLine.Command(
        name = "info"
)
public class Info extends Manage {

    @CommandLine.Command(name = "version")
    public Integer version() {
        return executeCDK("--version");
    }

    @CommandLine.Command(name = "which")
    public Integer which() {
        return executeProcess("which", kernel.cdk);
    }
}
