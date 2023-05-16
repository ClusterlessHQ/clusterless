/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.task.aws;

import clusterless.json.JSONUtil;
import clusterless.scenario.conductor.task.BaseSimpleTask;
import clusterless.scenario.model.WatchedStore;

public class S3Watcher extends BaseSimpleTask {

    public static final String AWS_S_3_WATCHER = "awsS3Watcher";

    public S3Watcher(String taskReferenceName, WatchedStore store) {
        super(AWS_S_3_WATCHER, taskReferenceName);

        input("input", JSONUtil.writeAsStringSafe(store));
    }
}
