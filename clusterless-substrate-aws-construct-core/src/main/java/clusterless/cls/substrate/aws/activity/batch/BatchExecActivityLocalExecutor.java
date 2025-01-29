/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.activity.batch;

import clusterless.cls.managed.component.ActivityLocalExecutor;
import clusterless.cls.managed.component.ExecCommand;
import clusterless.cls.substrate.aws.common.batch.BatchLocalExecutor;
import clusterless.cls.substrate.aws.common.batch.BatchPayloadCommand;
import clusterless.cls.substrate.aws.construct.IsScheduled;
import clusterless.cls.util.Env;
import software.amazon.awscdk.services.events.Schedule;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BatchExecActivityLocalExecutor implements ActivityLocalExecutor, BatchLocalExecutor, IsScheduled {

    private final BatchExecActivity activity;

    public BatchExecActivityLocalExecutor(BatchExecActivity activity) {
        this.activity = activity;
    }

    @Override
    public List<ExecCommand> commands() {

        Schedule scheduleFrom = createScheduleFrom(activity.schedule());

        if (scheduleFrom == null) {
            throw new IllegalArgumentException("Invalid schedule: " + activity.schedule());
        }

        ActivityProps activityProps = ActivityProps.Builder.builder()
                .withSchedule(activity.schedule()) // normalize the unit name
                .withPathURI(activity.pathURI()) // TODO: resolve the path URI from the ref
                .build();

        Map<String, String> environment = new LinkedHashMap<>(activity.environment());
        environment.putAll(Env.toEnv(activityProps));

        Map<String, String> localComments = new LinkedHashMap<>();
        Map<String, String> localEnvironment = new LinkedHashMap<>(environment);

        localComments.put("CLS_ACTIVITY_PROPS_JSON", "provides all project metadata");
        localComments.put("CLS_ACTIVITY_PROPS_JAVA", "same as CLS_ACT_PROPS_JSON but for loading into an Java object via Jackson");

        addProvided(localComments, localEnvironment);
        addHelper(localComments, localEnvironment);

        BatchPayloadCommand payloadCommand = new BatchPayloadCommand(activity.command());

        String commandComments = String.format("declared: %s\npayload: %s", payloadCommand.declared(), payloadCommand.payload());
        List<String> localArguments = payloadCommand.declared();

        ExecCommand command = ExecCommand.builder()
                .withHeaderComment("CLS_* variables are provided by the Clusterless framework")
                .withEnvironmentComments(localComments)
                .withEnvironment(localEnvironment)
                .withCommandComment(commandComments)
                .withCommand(localArguments)
                .build();

        return List.of(command);
    }
}
