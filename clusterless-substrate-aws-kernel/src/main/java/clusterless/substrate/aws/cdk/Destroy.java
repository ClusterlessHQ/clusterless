/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;


import clusterless.command.DestroyCommandOptions;
import clusterless.substrate.aws.ProcessExec;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "destroy"
)
public class Destroy extends CommonCommand implements Callable<Integer> {
    @CommandLine.Mixin
    ProcessExec processExec = new ProcessExec();
    @CommandLine.Mixin
    DestroyCommandOptions commandOptions = new DestroyCommandOptions();

    @Override
    public Integer call() throws Exception {

        return processExec.executeLifecycleProcess("destroy", commandOptions);
    }
}
