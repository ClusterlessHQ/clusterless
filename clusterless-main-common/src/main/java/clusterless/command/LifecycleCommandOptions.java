/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.command;


import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class LifecycleCommandOptions extends CommonCommandOptions {
    @CommandLine.Option(
            names = {"-p", "--project"},
            description = "the files that declare the project to be deployed"
    )
    List<File> projectFiles = new ArrayList<>();

    public List<File> projectFiles() {
        return projectFiles;
    }

}
