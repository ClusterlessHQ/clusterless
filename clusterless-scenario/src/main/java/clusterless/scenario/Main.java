/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario;

import clusterless.cls.json.JSONUtil;
import clusterless.scenario.conductor.ConductorApp;
import clusterless.scenario.conductor.TaskManager;
import clusterless.scenario.conductor.WorkflowManager;
import clusterless.scenario.conductor.runner.BootstrapRunner;
import clusterless.scenario.conductor.runner.ScenarioRunner;
import clusterless.scenario.model.Scenario;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.run.Workflow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(
        name = "cls-scenario",
        mixinStandardHelpOptions = true,
        version = "1.0-wip",
        subcommands = {
                CommandLine.HelpCommand.class,
        }
)
public class Main implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    private ConfigurableApplicationContext server;

    @CommandLine.Mixin
    Options options = new Options();

    @CommandLine.Option(names = "--server")
    String serverHostPort;

    @CommandLine.Option(names = "--stop-on-failure")
    boolean stopOnFailure = false;

    @CommandLine.Option(names = {"-f", "--scenarios"})
    Path scenarios;

    public Main() {
    }

    public void server() {
        server = ConductorApp.run();
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Main());

        commandLine.parseArgs(args);

        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }

        System.exit(commandLine.execute(args));
    }

    @Override
    public Integer call() throws Exception {
        List<Path> paths;
        try (Stream<Path> pathStream = Files.find(this.scenarios, 20, (p, a) -> p.getFileName().toString().equals("scenario.json"))) {
            paths = pathStream.map(Path::normalize).collect(Collectors.toList());
        }

        LOG.info("found paths: {}", paths);

        List<Scenario> scenarios = paths.stream()
                .map(Main::scenario)
                .toList();

        if (serverHostPort == null) {
            server();
        }

        // bootstrap
        List<Map<String, String>> placements = scenarios.stream()
                .filter(Scenario::enabled)
                .flatMap(s -> s.placements().stream())
                .distinct()
                .toList();

        int totalFailedFlows = 0;

        TaskManager taskManager = null;

        try {
            WorkflowManager workflowManager = new WorkflowManager(serverHostPort);

            taskManager = new TaskManager(options, workflowManager);

            Set<String> startedBootstrapDeploys = startBootstrapDeployExecution(placements, workflowManager);
            int totalFailedBootstrapDeploys = monitorFlow(workflowManager, startedBootstrapDeploys);

            if (totalFailedBootstrapDeploys > 0) {
                LOG.error("boostrap deploy failed");
                return totalFailedBootstrapDeploys;
            }

            Set<String> startedScenarios = startScenarioExecution(scenarios, workflowManager);

            totalFailedFlows = monitorFlow(workflowManager, startedScenarios);

            Set<String> startedBootstrapDestroys = startBootstrapDestroyExecution(placements, workflowManager);
            int totalFailedBootstrapDestroys = monitorFlow(workflowManager, startedBootstrapDestroys);

            if (totalFailedBootstrapDestroys > 0) {
                LOG.error("boostrap destroy failed");
                return totalFailedBootstrapDestroys;
            }
        } finally {
            LOG.info("shutting down services");

            if (taskManager != null) {
                taskManager.shutdown();
            }

            if (server != null) {
                server.stop();
            }
        }

        return totalFailedFlows;
    }

    @NotNull
    private Set<String> startBootstrapDeployExecution(List<Map<String, String>> placements, WorkflowManager workflowManager) {
        return startBootstrapExecution(placements, workflowManager, false);
    }

    @NotNull
    private Set<String> startBootstrapDestroyExecution(List<Map<String, String>> placements, WorkflowManager workflowManager) {
        return startBootstrapExecution(placements, workflowManager, true);
    }

    @NotNull
    private Set<String> startBootstrapExecution(List<Map<String, String>> placements, WorkflowManager workflowManager, boolean destroy) {
        List<BootstrapRunner> runners = placements.stream()
                .map(s -> new BootstrapRunner(options, workflowManager, s, destroy, scenarios))
                .toList();

        return runners.stream()
                .map(BootstrapRunner::exec)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    @NotNull
    private Set<String> startScenarioExecution(List<Scenario> scenarios, WorkflowManager workflowManager) {
        List<ScenarioRunner> runners = scenarios.stream()
                .filter(Scenario::enabled)
                .map(s -> new ScenarioRunner(options, workflowManager, s))
                .toList();

        return runners.stream()
                .map(ScenarioRunner::exec)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private int monitorFlow(WorkflowManager workflowManager, Set<String> started) throws InterruptedException {
        int totalFailedFlows = 0;
        int totalFlows = started.size();

        LOG.info("started flows: {}", started.size());

        long time = System.currentTimeMillis();
        while (true) {
            List<Workflow> workflows = started.stream()
                    .map(i -> workflowManager.workflowClient().getWorkflow(i, true))
                    .toList();

            Set<String> completed = workflows.stream()
                    .filter(w -> w.getStatus() == Workflow.WorkflowStatus.COMPLETED)
                    .map(Workflow::getWorkflowId)
                    .collect(Collectors.toSet());

            Set<String> failed = workflows.stream()
                    .filter(w -> w.getStatus() == Workflow.WorkflowStatus.FAILED)
                    .map(Workflow::getWorkflowId)
                    .collect(Collectors.toSet());

            started.removeAll(completed);
            started.removeAll(failed);

            totalFailedFlows = failed.size();

            if (stopOnFailure && !failed.isEmpty()) {
                LOG.error("shutting down, flows running, {}, failed: {}", started.size(), totalFailedFlows);
                break;
            }

            if (started.isEmpty()) {
                LOG.info("completed all flows");
                break;
            }

            if (System.currentTimeMillis() - time > 10000) {
                for (Workflow workflow : workflows) {
                    if (workflow.getStatus() == Workflow.WorkflowStatus.FAILED) {
                        LOG.info("workflow: {}, status: {}", workflow.getWorkflowName(), workflow.getStatus());
                    } else if (workflow.getStatus() == Workflow.WorkflowStatus.RUNNING) {
                        LOG.info("workflow: {}, status: {}", workflow.getWorkflowName(), workflow.getStatus());
                        for (Task task : workflow.getTasks()) {
                            if (task.getStatus().isTerminal()) {
                                long duration = task.getEndTime() - task.getStartTime();
                                LOG.info("task: {}, status: {}, duration: {}", task.getReferenceTaskName(), task.getStatus(), Duration.ofMillis(duration));
                            } else {
                                long duration = task.getStatus() == Task.Status.IN_PROGRESS ? System.currentTimeMillis() - task.getStartTime() : 0;
                                LOG.info("task: {}, status: {}, duration: {}", task.getReferenceTaskName(), task.getStatus(), Duration.ofMillis(duration));
                            }
                        }
                    }
                }
                time = System.currentTimeMillis();
            }

            Thread.sleep(1000);
        }

        LOG.info("started flows: {}, succeeded: {}, failed: {}", totalFlows, totalFlows - totalFailedFlows, totalFailedFlows);

        return totalFailedFlows;
    }

    private static Scenario scenario(Path p) {
        return JSONUtil.readObjectSafe(p, Scenario.class).setProjectDirectory(p.getParent());
    }
}
