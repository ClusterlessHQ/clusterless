/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.batch;

import clusterless.json.JSONUtil;
import clusterless.managed.component.ArcLocalExecutor;
import clusterless.model.deploy.Placement;
import clusterless.substrate.aws.arc.props.ArcEnvBuilder;
import clusterless.substrate.aws.event.ArcWorkloadContext;
import clusterless.util.Runtimes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BatchExecArcLocalExecutor implements ArcLocalExecutor {

    /**
     * https://docs.aws.amazon.com/step-functions/latest/dg/connect-to-resource.html#connect-wait-token
     */
    static String stepContext = "{\n" +
                                "    \"Execution\": {\n" +
                                "        \"Id\": \"arn:aws:states:us-east-1:123456789012:execution:stateMachineName:executionName\",\n" +
                                "        \"Input\": {\n" +
                                "           \"key\": \"value\"\n" +
                                "        },\n" +
                                "        \"Name\": \"executionName\",\n" +
                                "        \"RoleArn\": \"arn:aws:iam::123456789012:role...\",\n" +
                                "        \"StartTime\": \"2019-03-26T20:14:13.192Z\"\n" +
                                "    },\n" +
                                "    \"State\": {\n" +
                                "        \"EnteredTime\": \"2019-03-26T20:14:13.192Z\",\n" +
                                "        \"Name\": \"Test\",\n" +
                                "        \"RetryCount\": 3\n" +
                                "    },\n" +
                                "    \"StateMachine\": {\n" +
                                "        \"Id\": \"arn:aws:states:us-east-1:123456789012:stateMachine:stateMachineName\",\n" +
                                "        \"Name\": \"name\"\n" +
                                "    },\n" +
                                "    \"Task\": {\n" +
                                "        \"Token\": \"h7XRiCdLtd/83p1E0dMccoxlzFhglsdkzpK9mBVKZsp7d9yrT1W\"\n" +
                                "    }\n" +
                                "}";

    private final Placement placement;
    private final BatchExecArc arc;

    public BatchExecArcLocalExecutor(Placement placement, BatchExecArc arc) {
        this.placement = placement;
        this.arc = arc;
    }

    @Override
    public List<Command> commands(String role, String lotId) {
        ArcEnvBuilder arcEnvBuilder = new ArcEnvBuilder(placement, arc);
        Map<String, String> arcEnvironment = arcEnvBuilder.asEnvironment();
        ArcWorkloadContext arcWorkloadContext = arcEnvBuilder.execContext(role, lotId);

        Map<String, String> localComments = new LinkedHashMap<>();
        Map<String, String> localEnvironment = new LinkedHashMap<>(arcEnvironment);

        localComments.put("CLS_ARC_PROPS_JSON", "provides all project metadata");
        localComments.put("CLS_ARC_PROPS_JAVA", "same as CLS_ARC_PROPS_JSON but for loading into an Java object via Jackson");

        addProvided(localComments, localEnvironment);
//        addHelper(localComments, localEnvironment);

        BatchPayloadCommand payloadCommand = new BatchPayloadCommand(arc.workload().command());

        String commandComments = String.format("declared: %s\npayload: %s", payloadCommand.declared(), payloadCommand.payload());
        List<String> localArguments = payloadCommand.fill(stepContext, JSONUtil.writeAsStringSafe(arcWorkloadContext));

        Command command = Command.builder()
                .withHeaderComment("CLS_* variables are provided by the Clusterless framework")
                .withEnvironmentComments(localComments)
                .withEnvironment(localEnvironment)
                .withCommandComment(commandComments)
                .withCommand(localArguments)
                .build();

        return List.of(command);
    }

    private static void addProvided(Map<String, String> localComments, Map<String, String> localEnvironment) {
        localComments.put("AWS_BATCH_JOB_ID", "AWS_BATCH_JOB_ID and AWS_BATCH_JOB_ATTEMPT are provided by AWS Batch");
        localEnvironment.put("AWS_BATCH_JOB_ID", "00000000-0000-0000-0000-000000000000");
        localEnvironment.put("AWS_BATCH_JOB_ATTEMPT", "1");
    }

    private static void addHelper(Map<String, String> localComments, Map<String, String> localEnvironment) {
        localComments.put("CLS_ATTEMPT", "use for manifest {attempt} value, 0 padding added to JOB_ATTEMPT");
        localEnvironment.put("CLS_ATTEMPT", "${AWS_BATCH_JOB_ID}-$(printf %03d ${AWS_BATCH_JOB_ATTEMPT})");

        // millis on linux: echo $(($(date +%s%N)/1000000)) or echo $(date +%s%3N)
        //        on osx echo $(date +%s000)
        localComments.put("CLS_TIMESTAMP", "may not work on Alpine Linux");
        localEnvironment.put("CLS_TIMESTAMP", Runtimes.choose("$(date +%s000)", "0000000000000", "$(date +%s%3N"));
        localComments.put("CLS_OBJECT_GUID", "use as prefix to any stored object name");
        localEnvironment.put("CLS_OBJECT_GUID", "${CLS_ATTEMPT}-${CLS_TIMESTAMP}");
    }
}