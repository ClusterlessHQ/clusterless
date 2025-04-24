/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.sdk;

import clusterless.cls.json.JSONUtil;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;

public class StepFunction extends ClientBase<SfnClient> {
    public StepFunction(String profile) {
        super(profile);
    }

    public StepFunction(String profile, String region) {
        super(profile, region);
    }

    @Override
    protected @NotNull String getEndpointEnvVar() {
        return "AWS_SFN_ENDPOINT";
    }

    @Override
    protected SfnClient createClient(String region) {
        logEndpointOverride();

        return SfnClient.builder()
                .region(region == null ? null : Region.of(region)) // allows sdk to lookup region in chain
                .credentialsProvider(credentialsProvider)
                .endpointOverride(endpointOverride)
                .build();
    }

    public ClientBase<SfnClient>.Response start(String account, String functionName, Object value) {
        String body = JSONUtil.writeAsStringSafe(value);

        // arn:aws:states:us-west-2:xxx:stateMachine:DEV-foo-20250101
        String arn = "arn:aws:states:%s:%s:stateMachine:%s".formatted(region, account, functionName);

        StartExecutionRequest request = StartExecutionRequest.builder()
                .stateMachineArn(arn)
                .input(body)
                .build();

        try (SfnClient client = createClient()) {
            return new Response(client.startExecution(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }
}
