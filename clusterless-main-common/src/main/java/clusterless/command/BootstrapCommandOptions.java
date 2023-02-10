/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.command;

import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(description = "initialize the given environment")
public class BootstrapCommandOptions extends CommandOptions {

    @CommandLine.Option(
            names = {"--account"}
    )
    String account;
    @CommandLine.Option(
            names = {"--region"}
    )
    String region;

    @CommandLine.Option(
            names = {"--stage"}
    )
    String stage;

    @CommandLine.Option(
            names = {"--synth"},
            hidden = true
    )
    boolean synth = false;

    public String account() {
        return account;
    }

    public String region() {
        return region;
    }

    public String stage() {
        return stage;
    }

    public boolean synth() {
        return synth;
    }
}
