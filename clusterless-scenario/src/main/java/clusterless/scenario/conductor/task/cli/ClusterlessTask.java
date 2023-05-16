/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.task.cli;

import clusterless.scenario.conductor.task.BaseSimpleTask;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ClusterlessTask extends BaseSimpleTask {
    public ClusterlessTask(String taskDefName, String taskReferenceName, Path workingDirectory, List<Path> projectFiles) {
        super(taskDefName, taskReferenceName);

        input("workingDirectory", workingDirectory.toAbsolutePath().toString());
        input("projectFiles", projectFiles.stream().map(Path::toString).collect(Collectors.joining(",")));
    }

}
