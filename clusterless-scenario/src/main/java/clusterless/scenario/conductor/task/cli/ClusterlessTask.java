package clusterless.scenario.conductor.task.cli;

import clusterless.scenario.conductor.task.BaseSimpleTask;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ClusterlessTask extends BaseSimpleTask {
    public ClusterlessTask(String taskDefName, String taskReferenceName, Path workingDirectory, List<Path> projectFiles) {
        super(taskDefName, taskReferenceName);

        input("workingDirectory", workingDirectory.toAbsolutePath().toString());
        input("projectFiles", projectFiles.stream().map(Path::toString).collect(Collectors.joining(",")));
    }

}
