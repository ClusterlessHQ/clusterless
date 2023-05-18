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
import java.util.Map;

public class ClusterlessBootstrapTask extends BaseSimpleTask {
    public ClusterlessBootstrapTask(String taskDefName, String taskReferenceName, Path workingDirectory, Map<String, String> placement) {
        super(taskDefName, taskReferenceName);

        input("workingDirectory", workingDirectory.toAbsolutePath().toString());
        input("placement", placement);
    }
}
