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
@CommandLine.Command(
        description = "",
        subcommands = {CommandLine.HelpCommand.class}
)
public class ReportCommandOptions extends CommonCommandOptions {
    @CommandLine.Option(
            names = "--profile",
            description = "AWS profile."
    )
    private String profile = System.getenv("AWS_PROFILE");
    @CommandLine.Option(
            names = {"--account"},
            description = "Filter results by account."
    )
    String account;
    @CommandLine.Option(
            names = {"--region"},
            description = "Filter results by region."
    )
    String region;
    @CommandLine.Option(
            names = {"--stage"},
            description = "Filter results by stage.",
            fallbackValue = "",
            arity = "0..1"
    )
    String stage;

    public String profile() {
        return profile;
    }

    public String account() {
        return account;
    }

    public String region() {
        return region;
    }

    public String stage() {
        return stage;
    }
}
