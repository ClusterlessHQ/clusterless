/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.report;

import picocli.CommandLine;

import java.util.LinkedList;
import java.util.List;

public class ArcsCommandOptions extends ReportCommandOptions {
    @CommandLine.Option(
            names = {"--project"},
            description = "Filter results by project, and optionally version. (e.g. 'project:version')"
    )
    List<String> projects = new LinkedList<>();

    public List<String> projects() {
        return projects;
    }
}
