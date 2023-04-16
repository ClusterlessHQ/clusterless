package clusterless.scenario.conductor.runner;

import clusterless.scenario.conductor.WorkflowManager;
import clusterless.scenario.conductor.task.Deployer;
import clusterless.scenario.conductor.task.Destroyer;
import clusterless.scenario.model.Scenario;
import com.netflix.conductor.common.metadata.workflow.StartWorkflowRequest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;

import java.util.List;

public class ScenarioRunner {
    private final WorkflowManager workflowManager;
    private final Scenario scenario;
    private String workflowId;

    public ScenarioRunner(WorkflowManager workflowManager, Scenario scenario) {
        this.workflowManager = workflowManager;
        this.scenario = scenario;
    }

    public String workflowId() {
        return workflowId;
    }

    public String exec() {
        WorkflowDef workflowDefinition = new WorkflowDef();
        workflowDefinition.setName(scenario.name());
        workflowDefinition.setDescription(scenario.description());
        workflowDefinition.setOwnerEmail("sample@sample.com");
        List<WorkflowTask> tasks = workflowDefinition.getTasks();

        tasks.addAll(new Deployer("clsDeployer", scenario.projectDirectory(), scenario.projectFiles()).getWorkflowDefTasks());
        tasks.addAll(new Destroyer("clsDestroyer", scenario.projectDirectory(), scenario.projectFiles()).getWorkflowDefTasks());

        StartWorkflowRequest workflowRequest =
                new StartWorkflowRequest()
                        .withName(scenario.name())
                        .withWorkflowDef(workflowDefinition);

        workflowId = workflowManager.workflowClient().startWorkflow(workflowRequest);

        return workflowId;
    }
}
