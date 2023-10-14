/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.task.aws;

import clusterless.cls.json.JSONUtil;
import clusterless.scenario.conductor.task.WatcherTask;
import clusterless.scenario.model.WatchedStore;

public class GlueWatcher extends WatcherTask {

    public static final String AWS_GLUE_WATCHER = "awsGlueWatcher";

    public GlueWatcher(String taskReferenceName, WatchedStore store) {
        super(AWS_GLUE_WATCHER, taskReferenceName);

        input("input", JSONUtil.writeAsStringSafe(store));
    }
}
