package clusterless.scenario.conductor.task;

import java.nio.file.Path;
import java.util.List;

public class Deployer extends ClusterlessTask {
    public Deployer(String taskDefName, Path workingDirectory, List<Path> projectFiles) {
        super("clsProjectDeployer", taskDefName, workingDirectory, projectFiles);
    }
}
