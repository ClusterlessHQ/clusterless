/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.runner;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.WorkflowManager;
import clusterless.scenario.conductor.task.cli.DeployerBootstrap;
import clusterless.scenario.conductor.task.cli.DestroyerBootstrap;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BootstrapRunner {
    private static final Logger LOG = LogManager.getLogger(BootstrapRunner.class);
    private final Options options;
    private final WorkflowManager workflowManager;
    private final Map<String, String> placement;
    private final boolean destroy;
    private final Path rootPath;
    private final String name;
    private String workflowId;

    public BootstrapRunner(Options options, WorkflowManager workflowManager, Map<String, String> placement, boolean destroy, Path rootPath) {
        this.options = options;
        this.workflowManager = workflowManager;
        this.placement = placement;
        this.destroy = destroy;
        this.rootPath = rootPath;

        this.name = String.format("%s-%s-%s", Objects.requireNonNull(placement.get("account")), Objects.requireNonNull(placement.get("region")), Objects.toString(placement.get("stage"), "none"));
    }

    public String workflowId() {
        return workflowId;
    }

    @NotNull
    private Path workingDirectory() {
        return rootPath.resolve(name);
    }

    public String exec() {
        WorkflowDef workflowDefinition = new WorkflowDef();
        workflowDefinition.setName(name);
        workflowDefinition.setDescription("managing bootstrap");
        workflowDefinition.setOwnerEmail("sample@sample.com");
        List<WorkflowTask> tasks = workflowDefinition.getTasks();

        if (!destroy) {
            applyBootstrapDeploy(tasks, name);
        } else {
            applyBootstrapDestroy(tasks, name);
        }

        StartWorkflowRequest workflowRequest =
                new StartWorkflowRequest()
                        .withName(name)
                        .withWorkflowDef(workflowDefinition);

        if (tasks.isEmpty()) {
            return null;
        }

        workflowId = workflowManager.workflowClient().startWorkflow(workflowRequest);

        return workflowId;
    }

    private void applyBootstrapDeploy(List<WorkflowTask> tasks, String name) {
        if (options.verifyOnly()) {
            LOG.info("bootstrap deploy: {}, skipping deployer by request", name);
            return;
        }

        LOG.info("boostrap deployer: {}", name);
        tasks.addAll(new DeployerBootstrap("clsBootstrapDeployer", workingDirectory(), placement).getWorkflowDefTasks());
    }

    private void applyBootstrapDestroy(List<WorkflowTask> tasks, String name) {
        if (options.disableDestroy()) {
            LOG.info("bootstrap destroy: {}, skipping destroyer by request", name);
            return;
        }

        if (options.verifyOnly()) {
            LOG.info("bootstrap deploy: {}, skipping destroyer by request", name);
            return;
        }

        LOG.info("boostrap destroy: {}", name);
        tasks.addAll(new DestroyerBootstrap("clsBootstrapDestroyer", workingDirectory(), placement).getWorkflowDefTasks());
    }
}
