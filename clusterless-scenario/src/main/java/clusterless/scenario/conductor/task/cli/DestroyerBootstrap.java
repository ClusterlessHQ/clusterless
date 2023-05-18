/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.task.cli;

import java.nio.file.Path;
import java.util.Map;

public class DestroyerBootstrap extends ClusterlessBootstrapTask {
    public static final String CLS_PLACEMENT_DESTROY = "clsPlacementDestroy";

    public DestroyerBootstrap(String taskReferenceName, Path workingDirectory, Map<String, String> placement) {
        super(CLS_PLACEMENT_DESTROY, taskReferenceName, workingDirectory, placement);
    }
}
