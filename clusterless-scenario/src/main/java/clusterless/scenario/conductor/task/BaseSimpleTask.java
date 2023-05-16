/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

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
