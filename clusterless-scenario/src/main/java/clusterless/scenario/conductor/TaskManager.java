package clusterless.scenario.conductor;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.worker.aws.S3IngressWorker;
import clusterless.scenario.conductor.worker.aws.S3WatcherWorker;
import clusterless.scenario.conductor.worker.cli.DeployerWorker;
import clusterless.scenario.conductor.worker.cli.DestroyerWorker;
import com.netflix.conductor.client.automator.TaskRunnerConfigurer;
import com.netflix.conductor.client.worker.Worker;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskManager {

    private final TaskRunnerConfigurer configurer;
    private final int threadCount = 10;

    public TaskManager(Options options, WorkflowManager manager) {
        List<Worker> workers = List.of(
                new DeployerWorker(options),
                new DestroyerWorker(options),
                new S3IngressWorker(options),
                new S3WatcherWorker(options)
        );

        Map<String, Integer> counts = workers.stream().map(Worker::getTaskDefName).collect(Collectors.toMap(k -> k, v -> threadCount));

        configurer = new TaskRunnerConfigurer.Builder(manager.taskClient, workers)
                .withTaskThreadCount(counts)
                .withShutdownGracePeriodSeconds(1)
                .build();

        configurer.init();
    }

    public void shutdown() {
        configurer.shutdown();
    }
}
