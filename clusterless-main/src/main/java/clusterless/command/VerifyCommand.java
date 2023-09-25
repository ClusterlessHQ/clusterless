/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.command;

import clusterless.CommandWrapper;
import clusterless.command.project.VerifyCommandOptions;
import picocli.CommandLine;

@CommandLine.Command(
        name = "verify",
        description = "verify project changes with a provider"
)
public class VerifyCommand extends CommandWrapper {
    public VerifyCommand() {
        super(new VerifyCommandOptions());
    }
}
