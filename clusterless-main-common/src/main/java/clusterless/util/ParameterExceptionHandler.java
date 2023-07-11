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

public class ParameterExceptionHandler implements CommandLine.IParameterExceptionHandler {
    private final Startup startup;

    public ParameterExceptionHandler(Startup startup) {
        this.startup = startup;
    }

    @Override
    public int handleParseException(CommandLine.ParameterException ex, String[] args) throws Exception {
        ex.getCommandLine().getErr().println(ex.getCommandLine().getColorScheme().errorText(ex.getMessage()));

        return CommandLine.ExitCode.USAGE;
    }
}
