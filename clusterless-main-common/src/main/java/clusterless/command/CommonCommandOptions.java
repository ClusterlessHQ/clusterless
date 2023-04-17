/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.command;


import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(
        subcommands = {CommandLine.HelpCommand.class}
)
public class CommonCommandOptions {
    @CommandLine.Option(names = "--dry-run", description = "do not execute underlying cdk binary")
    private boolean dryRun = false;

    public boolean dryRun() {
        return dryRun;
    }
}
