package clusterless.scenario.conductor.worker.cli;

import clusterless.scenario.Options;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;

public abstract class ClusterlessWorker implements Worker {
    private static final Logger LOG = LogManager.getLogger(ClusterlessWorker.class);
    private final String command;
    protected final Options options;

    public ClusterlessWorker(String command, Options options) {
        this.command = command;
        this.options = options;
    }

    @Override
    public TaskResult execute(Task task) {
        Map<String, Object> inputData = task.getInputData();

        task.setStatus(Task.Status.IN_PROGRESS);

        String workingDirectory = Objects.toString(inputData.get("workingDirectory"), null);
        String projectFiles = Objects.toString(inputData.get("projectFiles"), null);

        ClusterlessExecutor deploy = ClusterlessExecutor.builder()
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
