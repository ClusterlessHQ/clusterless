/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import picocli.CommandLine;

import java.io.FileNotFoundException;

/**
 *
 */
public class ExitCodeExceptionMapper implements CommandLine.IExitCodeExceptionMapper {

    public static final int FILE_NOT_FOUND = 74;

    @Override
    public int getExitCode(Throwable exception) {
        if (exception instanceof FileNotFoundException) {
            return FILE_NOT_FOUND;
        }

        if (exception instanceof ExitCodeException) {
            return ((ExitCodeException) exception).exitCode();
        }

        return CommandLine.ExitCode.SOFTWARE;
    }
}
