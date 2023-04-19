package clusterless.scenario.conductor.task;

import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.SimpleTask;

public class BaseSimpleTask extends SimpleTask {
    public BaseSimpleTask(String taskDefName, String taskReferenceName) {
        super(taskDefName, taskReferenceName);
    }

    @Override
    protected void updateWorkflowTask(WorkflowTask workflowTask) {
        super.updateWorkflowTask(workflowTask);

        workflowTask.setRetryCount(0);
    }
}
