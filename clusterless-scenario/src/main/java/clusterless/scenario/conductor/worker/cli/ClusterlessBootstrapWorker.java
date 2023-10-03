/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.cli;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.worker.cli.exec.ClusterlessBootstrapExecutor;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;

public abstract class ClusterlessBootstrapWorker implements Worker {
    private static final Logger LOG = LogManager.getLogger(ClusterlessBootstrapWorker.class);
    protected final Options options;
    private final boolean destroy;

    public ClusterlessBootstrapWorker(boolean destroy, Options options) {
        this.destroy = destroy;
        this.options = options;
    }

    @Override
    public TaskResult execute(Task task) {
        Map<String, Object> inputData = task.getInputData();

        task.setStatus(Task.Status.IN_PROGRESS);

        String workingDirectory = Objects.toString(inputData.get("workingDirectory"), null);
        Map<String, String> placement = (Map<String, String>) inputData.get("placement");

        ClusterlessBootstrapExecutor executor = ClusterlessBootstrapExecutor.Builder.builder()
                .withClsApp(options.clsApp())
                .withDryRun(getDryRun())
                .withPlacement(placement)
                .withDestroy(destroy)
                .withWorkingDirectory(workingDirectory)
                .build();

        LOG.info("worker executing bootstrap, placement: {}, destroy: {}", placement, destroy);

        int exitCode = executor.exec();

        if (exitCode == 0) {
            LOG.info("worker executed bootstrap, with exit: {}", exitCode);

            return TaskResult.complete();
        }

        LOG.error("worker executed bootstrap, with exit: {}", exitCode);
        return TaskResult.failed("exit code: " + exitCode);
    }

    protected boolean getDryRun() {
        return options.dryRun();
    }
}
