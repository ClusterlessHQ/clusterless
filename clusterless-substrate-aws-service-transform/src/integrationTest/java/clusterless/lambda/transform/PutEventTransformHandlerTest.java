/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.json.JSONUtil;
import clusterless.lambda.LocalStackBase;
import clusterless.lambda.TestDatasets;
import clusterless.lambda.TestLots;
import clusterless.lambda.transform.json.AWSEvent;
import clusterless.model.manifest.ManifestState;
import clusterless.temporal.IntervalUnit;
import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.adelean.inject.resources.junit.jupiter.WithJacksonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 */
@TestWithResources
public class PutEventTransformHandlerTest extends LocalStackBase {
    @WithJacksonMapper
    static ObjectMapper objectMapper = JSONUtil.OBJECT_MAPPER;

    TestDatasets datasets;

    public TestDatasets datasets() {
        if (datasets == null) {
            datasets = new TestDatasets(defaultPlacement(), "main");
        }

        return datasets;
    }

    @Override
    protected TransformProps getProps() {
        return TransformProps.builder()
                .withLotSource(LotSource.eventTime)
                .withManifestCompletePath(datasets().manifestPathList(ManifestState.complete).get(0))
                .withManifestPartialPath(datasets().manifestPathList(ManifestState.partial).get(0))
                .withLotUnit(IntervalUnit.TWELFTHS.name())
                .withDataset(datasets().datasetList().get(0))
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

        String lotId = TestLots.COMMON_LOT;

        PutEventTransformObserver eventContext = mock();

        handler.handleEvent(event, context(), eventContext);

        verify(eventContext).applyIdentifierURI(URI.create("s3://DOC-EXAMPLE-BUCKET1/project/version/y=2023/m=12/d=31/data.json"));
        verify(eventContext).applyLotId(lotId);
        verify(eventContext).applyDatasetItemsSize(1);
        verify(eventContext).applyManifestURI(datasets().manifestPathList(ManifestState.complete).get(0).withLot(lotId).uri());
    }
}
