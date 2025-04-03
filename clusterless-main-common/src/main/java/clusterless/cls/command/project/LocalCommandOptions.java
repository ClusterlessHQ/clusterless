/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.project;

import clusterless.cls.model.manifest.ManifestState;
import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(description = "Enable execution of the specified arc locally for testing.")
public class LocalCommandOptions extends ProjectCommandOptions {
    @CommandLine.Option(
            names = {"-n", "--name"},
            description = "The arc or activity name to execute.",
            required = true
    )
    String name;

    @CommandLine.Option(
            names = {"-l", "--lot"},
            description = "The lot id of the manifest to source.",
            required = false
    )
    String lotId;

    @CommandLine.Option(
            names = {"-r", "--role"},
            description = "The dataset role to execute against.",
            required = false,
            defaultValue = "main"
    )
    String role;

    @CommandLine.Option(
            names = {"-s", "--manifest-state"},
            description = {
                    "Manifest state.",
                    "Values: ${COMPLETION-CANDIDATES} (default: ${DEFAULT-VALUE})"
            }
    )
    ManifestState manifestState = ManifestState.complete;

    @CommandLine.Option(
            names = {"-i", "--image"},
            description = {
                    "Run commands via the supplied container image."
            }
    )
    String image;

    @CommandLine.Option(
            names = {"-e", "--entrypoint"},
            description = {
                    "Prefix this to the commands, should be the script referenced by the Dockerfile entrypoint."
            }
    )
    String entryPoint;

    public String name() {
        return name;
    }

    public String lotId() {
        return lotId;
    }

    public String role() {
        return role;
    }

    public ManifestState manifestState() {
        return manifestState;
    }

    public String image() {
        return image;
    }

    public String entryPoint() {
        return entryPoint;
    }
}
