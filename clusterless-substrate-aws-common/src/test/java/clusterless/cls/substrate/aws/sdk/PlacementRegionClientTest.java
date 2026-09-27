/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.sdk;

import clusterless.cls.substrate.aws.io.ArcReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.StartExecutionRequest;
import software.amazon.awssdk.services.sfn.model.StartExecutionResponse;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Clients built from the CLI must use the placement's region, not the environment's, while
 * the no-arg readers used inside lambdas keep resolving from the environment.
 */
public class PlacementRegionClientTest {
    @Test
    void stepFunctionArnUsesTheGivenRegion() {
        SfnClient client = mock(SfnClient.class);

        when(client.startExecution(any(StartExecutionRequest.class)))
                .thenReturn((StartExecutionResponse) StartExecutionResponse.builder()
                        .sdkHttpResponse(SdkHttpResponse.builder().statusCode(200).build())
                        .build());

        StepFunction stepFunction = new StepFunction("profile", "eu-west-1") {
            @Override
            protected SfnClient createClient(String region) {
                return client;
            }
        };

        Assertions.assertTrue(stepFunction.start("000000000000", "prod-arc", Map.of("key", "value")).isSuccess());

        ArgumentCaptor<StartExecutionRequest> captor = ArgumentCaptor.forClass(StartExecutionRequest.class);
        verify(client).startExecution(captor.capture());

        Assertions.assertEquals("arn:aws:states:eu-west-1:000000000000:stateMachine:prod-arc", captor.getValue().stateMachineArn());
    }

    @Test
    void arcReaderUsesTheGivenRegion() {
        var reader = new ArcReader("profile", "eu-west-1") {
            String s3Region() {
                return s3.region();
            }
        };

        Assertions.assertEquals("eu-west-1", reader.s3Region());
    }

    @Test
    void noArgArcReaderResolvesRegionFromTheEnvironment() {
        var reader = new ArcReader() {
            String s3Region() {
                return s3.region();
            }
        };

        Assertions.assertEquals(ClientBase.defaultRegion, reader.s3Region());
    }
}
