package clusterless.scenario;

import clusterless.json.JSONUtil;
import clusterless.scenario.conductor.ConductorApp;
import clusterless.scenario.conductor.TaskManager;
import clusterless.scenario.conductor.WorkflowManager;
import clusterless.scenario.conductor.runner.ScenarioRunner;
import clusterless.scenario.model.Scenario;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.run.Workflow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
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

    static {
//        System.setProperty("org.springframework.boot.logging.LoggingSystem","org.springframework.boot.logging.log4j2.Log4J2LoggingSystem");
//        System.setProperty("org.springframework.boot.logging.LoggingSystem","org.springframework.boot.logging.Slf4JLoggingSystem");
    }

    private final String[] args;
    private ConfigurableApplicationContext server;

    @CommandLine.Mixin
    Options options = new Options();

    @CommandLine.Option(names = "--server")
    String serverHostPort;

    @CommandLine.Option(names = {"-f", "--scenarios"})
    Path scenarios;

    public Main(String[] args) {
        this.args = args;
    }

    public void server() {
        server = ConductorApp.run();
    }

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Main(args));

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
                .collect(Collectors.toList());

        if (serverHostPort == null) {
            server();
        }

        TaskManager taskManager = null;

        try {
            WorkflowManager workflowManager = new WorkflowManager(serverHostPort);

            taskManager = new TaskManager(options, workflowManager);

            List<ScenarioRunner> runners = scenarios.stream()
                    .filter(Scenario::enabled)
                    .map(s -> new ScenarioRunner(workflowManager, s))
                    .collect(Collectors.toList());

            Set<String> started = runners.stream()
                    .map(ScenarioRunner::exec)
                    .collect(Collectors.toSet());

            int totalFlows = started.size();
            int totalFailedFlows = 0;

            LOG.info("started flows: {}", started.size());

            long time = System.currentTimeMillis();
            while (true) {
                List<Workflow> workflows = started.stream()
                        .map(i -> workflowManager.workflowClient().getWorkflow(i, true))
                        .collect(Collectors.toList());

                Set<String> completed = workflows.stream()
                        .filter(w -> w.getStatus() == Workflow.WorkflowStatus.COMPLETED)
                        .map(Workflow::getWorkflowId)
                        .collect(Collectors.toSet());

                Set<String> failed = workflows.stream()
                        .filter(w -> w.getStatus() == Workflow.WorkflowStatus.FAILED)
                        .map(Workflow::getWorkflowId)
                        .collect(Collectors.toSet());

                started.removeAll(completed);

                if (!failed.isEmpty()) {
                    totalFailedFlows += failed.size();
                    LOG.error("flows running, {}, failed: {}", started.size(), totalFailedFlows);
                    break;
                }

                if (started.isEmpty()) {
                    LOG.info("completed all flows");
                    break;
                }

                if (System.currentTimeMillis() - time > 10000) {
                    for (Workflow workflow : workflows) {
                        if (workflow.getStatus() == Workflow.WorkflowStatus.RUNNING) {
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

            LOG.info("started flow: {}, succeeded: {}, failed: {}", totalFlows, totalFlows - totalFailedFlows, totalFailedFlows);

        } finally {
            LOG.info("shutting down services");

            if (taskManager != null) {
                taskManager.shutdown();
            }

            if (server != null) {
                server.stop();
            }

        }

        return 0;
    }

    private static Scenario scenario(Path p) {
        return JSONUtil.readObjectSafe(p, Scenario.class).setProjectDirectory(p.getParent());
    }
}
