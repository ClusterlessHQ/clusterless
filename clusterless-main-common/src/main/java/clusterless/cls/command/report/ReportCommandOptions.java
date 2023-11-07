/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.report;

import clusterless.cls.command.CommonCommandOptions;
import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(
        description = "",
        subcommands = {CommandLine.HelpCommand.class}
)
public abstract class ReportCommandOptions extends CommonCommandOptions {
    public abstract String profile();

    public abstract String account();

    public abstract String region();

    public abstract String stage();
}
