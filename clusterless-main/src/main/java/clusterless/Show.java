/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless;

import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(name = "show")
public class Show {
    @CommandLine.ParentCommand
    Main main;

    @CommandLine.Command(name = "providers", description = "show all available providers")
    public int providers() {

        main.printer().print(main.substratesOptions().available());

        return 0;
    }
}
