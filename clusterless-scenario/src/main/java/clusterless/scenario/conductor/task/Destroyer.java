package clusterless.scenario.conductor.task;

import java.nio.file.Path;
import java.util.List;

public class Destroyer extends ClusterlessTask {
    public Destroyer(String taskDefName, Path workingDirectory, List<Path> projectFiles) {
        super("clsProjectDestroyer", taskDefName, workingDirectory, projectFiles);
    }
}
