package clusterless.scenario.conductor.task.cli;

import java.nio.file.Path;
import java.util.List;

public class Deployer extends ClusterlessTask {

    public static final String CLS_PROJECT_DEPLOYER = "clsProjectDeployer";

    public Deployer(String taskDefName, Path workingDirectory, List<Path> projectFiles) {
        super(CLS_PROJECT_DEPLOYER, taskDefName, workingDirectory, projectFiles);
    }
}
