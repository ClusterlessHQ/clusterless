/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.cli;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.worker.cli.exec.ClusterlessProjectExecutor;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;

public abstract class ClusterlessProjectWorker implements Worker {
    private static final Logger LOG = LogManager.getLogger(ClusterlessProjectWorker.class);
    private final String command;
    protected final Options options;

    public ClusterlessProjectWorker(String command, Options options) {
        this.command = command;
        this.options = options;
    }

    @Override
    public TaskResult execute(Task task) {
        Map<String, Object> inputData = task.getInputData();

        task.setStatus(Task.Status.IN_PROGRESS);

        String workingDirectory = Objects.toString(inputData.get("workingDirectory"), null);
        String projectFiles = Objects.toString(inputData.get("projectFiles"), null);

        ClusterlessProjectExecutor deploy = ClusterlessProjectExecutor.Builder.builder()
                .withClsApp(options.clsApp())
                .withDryRun(getDryRun())
                .withCommand(command)
                .withWorkingDirectory(workingDirectory)
                .withProjectFiles(projectFiles)
                .build();

        LOG.info("worker executing command: {}", command);

        int exitCode = deploy.exec();

        LOG.info("worker executed command: {}, with exit: {}", command, exitCode);

        if (exitCode == 0) {
            return TaskResult.complete();
        }

        return TaskResult.failed("exit code: " + exitCode);
    }

    protected boolean getDryRun() {
        return options.dryRun();
    }
}
