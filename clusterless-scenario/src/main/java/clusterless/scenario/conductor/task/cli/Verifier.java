package clusterless.scenario.conductor.task.cli;

import java.nio.file.Path;
import java.util.List;

public class Verifier extends ClusterlessTask {

    public static final String CLS_PROJECT_VERIFIER = "clsProjectVerifier";

    public Verifier(String taskDefName, Path workingDirectory, List<Path> projectFiles) {
        super(CLS_PROJECT_VERIFIER, taskDefName, workingDirectory, projectFiles);
    }
}
