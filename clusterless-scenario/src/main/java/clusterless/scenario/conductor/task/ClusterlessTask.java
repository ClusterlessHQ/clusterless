package clusterless.scenario.conductor.task;

import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ClusterlessTask extends SimpleTask {
    public ClusterlessTask(String taskDefName, String taskReferenceName, Path workingDirectory, List<Path> projectFiles) {
        super(taskDefName, taskReferenceName);

        input("workingDirectory", workingDirectory.toAbsolutePath().toString());
        input("projectFiles", projectFiles.stream().map(Path::toString).collect(Collectors.joining(",")));
    }

    @Override
    protected void updateWorkflowTask(WorkflowTask workflowTask) {
        super.updateWorkflowTask(workflowTask);

        workflowTask.setRetryCount(0);
    }
}
