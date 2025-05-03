/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.cli;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.task.cli.DestroyerBootstrap;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;

public class DestroyerBootstrapWorker extends ClusterlessBootstrapWorker {
    public DestroyerBootstrapWorker(Options options) {
        super(true, options);
    }

    @Override
    public String getTaskDefName() {
        return DestroyerBootstrap.CLS_PLACEMENT_DESTROY;
    }

    @Override
    public TaskResult execute(Task task) {

        // we have an issue where the destroy operation begins before some logs
        // arrive in cloud watch. this forces the log group to re-appear
        // and then blocks the next test run
        // assuming this is a race condition, we should have a delay
        // todo: make this configurable
        if (!getDryRun()) {
            try {
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        return super.execute(task);
    }
}
