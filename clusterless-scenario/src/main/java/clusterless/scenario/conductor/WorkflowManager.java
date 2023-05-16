/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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

    public WorkflowManager(String hostPort) {
        if (hostPort == null) {
            init(api());
        } else {
            init("http://" + hostPort + "/api/");
        }
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
