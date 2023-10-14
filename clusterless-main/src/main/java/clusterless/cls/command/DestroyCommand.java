/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command;

import clusterless.cls.CommandWrapper;
import clusterless.cls.command.project.DestroyCommandOptions;
import picocli.CommandLine;

@CommandLine.Command(
        name = "destroy",
        description = "Destroy a project deployed a declared placement."
)
public class DestroyCommand extends CommandWrapper<DestroyCommandOptions> {
    public DestroyCommand() {
        super(new DestroyCommandOptions());
    }
}
