package clusterless.scenario.conductor.task.cli;

import java.nio.file.Path;
import java.util.List;

public class Destroyer extends ClusterlessTask {

    public static final String CLS_PROJECT_DESTROYER = "clsProjectDestroyer";

    public Destroyer(String taskDefName, Path workingDirectory, List<Path> projectFiles) {
        super(CLS_PROJECT_DESTROYER, taskDefName, workingDirectory, projectFiles);
    }
}
