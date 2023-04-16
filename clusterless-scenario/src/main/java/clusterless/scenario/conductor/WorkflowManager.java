package clusterless.scenario.conductor;

import com.netflix.conductor.client.http.EventClient;
import com.netflix.conductor.client.http.MetadataClient;
import com.netflix.conductor.client.http.TaskClient;
import com.netflix.conductor.client.http.WorkflowClient;

public class WorkflowManager {
    protected int port = 8881;

    protected String hostPort = "localhost:%d";

    protected TaskClient taskClient;
    protected WorkflowClient workflowClient;
    protected MetadataClient metadataClient;
    protected EventClient eventClient;

    public WorkflowManager() {
        init(api());
    }

    protected void init(String api) {
        taskClient = new TaskClient();
        taskClient.setRootURI(api);

        workflowClient = new WorkflowClient();
        workflowClient.setRootURI(api);

        metadataClient = new MetadataClient();
        metadataClient.setRootURI(api);

        eventClient = new EventClient();
        eventClient.setRootURI(api);
    }

    private String api() {
        return String.format("http://" + hostPort + "/api/", port);
    }

    public TaskClient taskClient() {
        return taskClient;
    }

    public WorkflowClient workflowClient() {
        return workflowClient;
    }

    public MetadataClient metadataClient() {
        return metadataClient;
    }

    public EventClient eventClient() {
        return eventClient;
    }
}
