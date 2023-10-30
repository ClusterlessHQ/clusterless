/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.show;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "provider",
        description = "Show all available providers."
)
public class ShowProviders extends ShowCommand.BaseShow implements Callable<Integer> {

    @Override
    public Integer handleList() throws Exception {
        showCommand.main.printer().println(showCommand.main.substratesOptions().availableNames());
        return 0;
    }
}
