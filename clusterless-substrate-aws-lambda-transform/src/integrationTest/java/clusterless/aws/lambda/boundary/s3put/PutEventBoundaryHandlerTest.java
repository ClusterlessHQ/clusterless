/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.boundary.s3put;

import clusterless.aws.lambda.LocalStackBase;
import clusterless.aws.lambda.TestDatasets;
import clusterless.aws.lambda.TestLots;
import clusterless.aws.lambda.transform.json.object.AWSEvent;
import clusterless.cls.json.JSONUtil;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.commons.temporal.IntervalUnit;
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
public class PutEventBoundaryHandlerTest extends LocalStackBase {
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
    protected S3PutBoundaryProps getProps() {
        return S3PutBoundaryProps.builder()
                .withLotSource(LotSource.eventTime)
                .withManifestCompletePath(datasets().manifestPathList(ManifestState.complete).get(0))
                .withManifestPartialPath(datasets().manifestPathList(ManifestState.partial).get(0))
                .withLotUnit(IntervalUnit.TWELFTHS.name())
                .withDataset(datasets().sinkDatasetListAsSink().get(0))
                .withEventBusName(eventBusName())
                .build();
    }

    @Test
    void invoke(
            @GivenJsonResource("eventbridge-object-created.json")
            AWSEvent event
    ) {
        Assertions.assertNotNull(event);

        PutEventBoundaryHandler handler = new PutEventBoundaryHandler();

        String lotId = TestLots.COMMON_LOT;

        PutEventBoundaryObserver eventContext = mock();

        handler.handleEvent(event, context(), eventContext);

        verify(eventContext).applyLotId(lotId);
        verify(eventContext).applyIdentifierURI(URI.create("s3://DOC-EXAMPLE-BUCKET1/project/version/y=2023/m=12/d=31/data.json"));
        verify(eventContext).applyDatasetItemsSize(1);
        verify(eventContext).applyManifestURI(datasets().manifestPathList(ManifestState.complete).get(0).withLot(lotId).uri());
    }
}
