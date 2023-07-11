/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import clusterless.startup.Startup;
import picocli.CommandLine;

public class ExecutionExceptionHandler implements CommandLine.IExecutionExceptionHandler {
    private final Startup startup;

    public ExecutionExceptionHandler(Startup startup) {
        this.startup = startup;
    }

    @Override
    public int handleExecutionException(Exception ex, CommandLine commandLine, CommandLine.ParseResult parseResult) throws Exception {
        CommandLine.Help.ColorScheme colorScheme = commandLine.getColorScheme();

        commandLine.getErr().println(colorScheme.errorText(ex.getMessage()));

        if (startup.verbosity().level() > 0) {
            commandLine.getErr().println();
            commandLine.getErr().println(colorScheme.richStackTraceString(ex));
        }

        return commandLine.getExitCodeExceptionMapper().getExitCode(ex);
    }
}
