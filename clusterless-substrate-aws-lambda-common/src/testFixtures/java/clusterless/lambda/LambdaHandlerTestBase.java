/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda;

import clusterless.model.Struct;
import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Project;
import com.amazonaws.services.lambda.runtime.Context;
import org.jetbrains.annotations.NotNull;

public abstract class LambdaHandlerTestBase {
    private Placement placement;
    private Project project;

    protected String defaultRegion() {
        return "us-east-2";
    }

    public Placement defaultPlacement() {
        if (placement == null) {
            placement = Placement.Builder.builder()
                    .withStage("prod")
                    .withRegion(defaultRegion())
                    .withAccount("0000000000")
                    .build();
        }
        return placement;
    }

    public Project defaultProject() {
        if (project == null) {
            project = Project.Builder.builder()
                    .withName("test-project")
                    .withVersion("20230101")
                    .build();
        }
        return project;
    }

    protected abstract Struct getProps();

    @NotNull
    protected String eventBusName() {
        return "forwarding-bus";
    }

    @NotNull
    protected String sqsQueueName() {
        return "event-queue";
    }

    protected Context context() {
        return null;
    }
}
