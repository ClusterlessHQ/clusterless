/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.json.JSONUtil;
import clusterless.lambda.transform.json.AWSEvent;
import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.adelean.inject.resources.junit.jupiter.WithJacksonMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 */
@TestWithResources
public class PutEventTransformHandlerTest {
    @WithJacksonMapper
    ObjectMapper objectMapper = JSONUtil.OBJECT_MAPPER;

    Context context;

    @Test
    void invoke(
            @GivenJsonResource("eventbridge-object-created.json")
            AWSEvent event
    ) {
        Assertions.assertNotNull(event);

        PutEventTransformHandler handler = new PutEventTransformHandler();

        ArcNotifyEvent result = handler.handleRequest(event, context);
    }
}
