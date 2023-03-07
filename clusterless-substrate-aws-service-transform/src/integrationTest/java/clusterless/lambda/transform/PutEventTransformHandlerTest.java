/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.json.JSONUtil;
import clusterless.lambda.BaseHandlerTest;
import clusterless.lambda.TestDatasets;
import clusterless.lambda.transform.json.AWSEvent;
import clusterless.temporal.IntervalUnit;
import clusterless.util.URIs;
import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.adelean.inject.resources.junit.jupiter.WithJacksonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;

/**
 *
 */
@TestWithResources
public class PutEventTransformHandlerTest extends BaseHandlerTest {
    @WithJacksonMapper
    static ObjectMapper objectMapper = JSONUtil.OBJECT_MAPPER;

    static TestDatasets testDatasets = new TestDatasets();

    @Override
    protected TransformProps getProps() {
        return TransformProps.Builder.builder()
                .withLotSource(LotSource.eventTime)
                .withManifestPath(testDatasets.manifestPathList().get(0))
                .withLotUnit(IntervalUnit.TWELFTHS.name())
                .withDataset(testDatasets.datasetList().get(0))
                .withEventBusName(eventBusName())
                .build();
    }

    @Test
    void invoke(
            @GivenJsonResource("eventbridge-object-created.json")
            AWSEvent event
    ) {
        Assertions.assertNotNull(event);

        PutEventTransformHandler handler = new PutEventTransformHandler();

        String lotId = "20211112PT5M000";

        PutEventTransformObserver eventContext = new PutEventTransformObserver() {
            @Override
            public void applyEvent(OffsetDateTime time, String bucket, String key) {
            }

            @Override
            public void applyIdentifierURI(URI identifierURI) {
                Assertions.assertEquals(URI.create("s3://DOC-EXAMPLE-BUCKET1/project/version/y=2023/m=12/d=31/data.json"), identifierURI);
            }

            @Override
            public void applyLotId(String value) {
                Assertions.assertEquals(lotId, value);
            }

            @Override
            public void applyDatasetItemsSize(int datasetItemsSize) {
                Assertions.assertEquals(1, datasetItemsSize);
            }

            @Override
            public void applyManifestURI(URI manifestURI) {
                Assertions.assertEquals(URIs.copyAppendPath(testDatasets.manifestPathList().get(0), "lot=" + lotId, "manifest.json"), manifestURI);
            }
        };

        handler.handleEvent(event, context(), eventContext);

    }
}
