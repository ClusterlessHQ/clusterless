/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.command;


import clusterless.util.StdInToFileConverter;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 */
public class ProjectCommandOptions extends CommonCommandOptions {
    @CommandLine.Option(
            names = {"-p", "--project"},
            description = "the files that declare the project to be deployed, or `-` to read from stdin",
            converter = StdInToFileConverter.class
    )
    List<File> projectFiles = new ArrayList<>();

    @CommandLine.Option(
            names = {"--exclude-arc"},
            description = "exclude the named arc from the deployment"
    )
    List<String> excludeArcNames = new ArrayList<>();

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @CommandLine.Option(
            names = "--exclude-all-arcs",
            description = "exclude all arcs from the deployment, only deploy resources and boundaries",
            defaultValue = CommandLine.Option.NULL_VALUE,
            fallbackValue = "true",
            arity = "0..1"
    )
    Optional<Boolean> excludeAllArcs;

    public List<File> projectFiles() {
        return projectFiles;
    }

    public List<String> excludeArcNames() {
        return excludeArcNames;
    }

    public Optional<Boolean> excludeAllArcs() {
        return excludeAllArcs;
    }
}
