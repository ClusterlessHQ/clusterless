/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command;

import clusterless.cls.Main;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 *
 */
@CommandLine.Command()
public class CommandWrapper<C extends CommonCommandOptions> implements Callable<Integer> {
    @CommandLine.ParentCommand
    Main main;

    @CommandLine.Mixin
    C commandOptions;

    public CommandWrapper(C commandOptions) {
        this.commandOptions = commandOptions;
    }

    @Override
    public Integer call() throws Exception {
        return main.run(this.commandOptions);
    }
}
