/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import picocli.CommandLine;

/**
 *
 */
//@CommandLine.Command(name="manage", hidden = true)
public class Manage {
    @CommandLine.ParentCommand
    Kernel kernel;

    public Manage() {
    }

    @CommandLine.Command(
            name = "verify"
    )
    public int verify() {
        System.out.println("verify");
        System.out.println("kernel.direct = " + kernel.direct);
        return 0;
    }

    @CommandLine.Command(
            name = "deploy"
    )
    public int deploy() {
        System.out.println("deploy");
        return 0;
    }
}
