/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.local;

import clusterless.command.LocalCommandOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 *
 */
@CommandLine.Command(
        name = "local"
)
public class Local implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Local.class);

    @CommandLine.Mixin
    LocalCommandOptions commandOptions = new LocalCommandOptions();
    //    @CommandLine.Mixin
    LocalProcessExec processExec = new LocalProcessExec(commandOptions);

    @Override
    public Integer call() throws Exception {
        return null;
    }
}
