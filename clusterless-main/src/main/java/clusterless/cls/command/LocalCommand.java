/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command;

import clusterless.cls.CommandWrapper;
import clusterless.cls.command.project.LocalCommandOptions;
import picocli.CommandLine;

@CommandLine.Command(
        name = "local",
        description = "Support for executing workloads locally in a terminal."
)
public class LocalCommand extends CommandWrapper<LocalCommandOptions> {
    public LocalCommand() {
        super(new LocalCommandOptions());
    }
}
