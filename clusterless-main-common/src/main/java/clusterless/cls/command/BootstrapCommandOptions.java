/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command;

import clusterless.cls.command.exec.ExecCommandOptions;
import picocli.CommandLine;

import java.util.Optional;

/**
 *
 */
@CommandLine.Command(description = "Initialize the given environment.")
public class BootstrapCommandOptions extends ExecCommandOptions {
    @CommandLine.Option(
            names = {"--destroy"},
            description = "Remove the bootstrap for the given placement."
    )
    boolean destroy = false;
    @CommandLine.Option(
            names = {"--account"},
            description = "AWS account."
    )
    String account;
    @CommandLine.Option(
            names = {"--region"},
            description = "AWS region."
    )
    String region;
    @CommandLine.Option(
            names = {"--stage"},
            arity = "0..1",
            description = {
                    "Optional stage prefix to use within the cloud placement.",
                    "For example: `dev`, `prod`, or `test`."
            }
    )
    String stage;
    @CommandLine.Option(
            names = {"--synth"},
            hidden = true
    )
    boolean synth = false;

    @CommandLine.Option(
            names = "--retry",
            description = "Retry the destroy operation.")
    private boolean retry = false;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @CommandLine.Option(
            names = "--approve",
            description = "Approve changes to be deployed.",
            defaultValue = CommandLine.Option.NULL_VALUE,
            fallbackValue = "true",
            arity = "0..1"
    )
    Optional<Boolean> approve;

    public boolean destroy() {
        return destroy;
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

    public boolean synth() {
        return synth;
    }

    public Optional<Boolean> approve() {
        return approve;
    }

    public boolean retry() {
        // only enable retry if destroy is enabled
        return destroy && retry;
    }
}
