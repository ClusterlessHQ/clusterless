/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.boundary.frequents3put;

import clusterless.aws.lambda.LocalStackBase;
import clusterless.aws.lambda.TestDatasets;
import clusterless.aws.lambda.TestLots;
import clusterless.aws.lambda.transform.json.event.AWSEvent;
import clusterless.cls.json.JSONUtil;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.substrate.aws.sdk.SQS;
import clusterless.commons.temporal.IntervalUnit;
import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.GivenTextResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.adelean.inject.resources.junit.jupiter.WithJacksonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 *
 */
@TestWithResources
public class FrequentPutEventBoundaryHandlerTest extends LocalStackBase {
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
    protected FrequentS3PutBoundaryProps getProps() {
        return FrequentS3PutBoundaryProps.builder()
                .withManifestCompletePath(datasets().manifestPathList(ManifestState.complete).get(0))
                .withManifestPartialPath(datasets().manifestPathList(ManifestState.partial).get(0))
                .withLotUnit(IntervalUnit.TWELFTHS.name())
                .withDataset(datasets().sinkDatasetListAsSink().get(0))
                .withEventBusName(eventBusName())
                .withSqsQueueName(sqsQueueName())
                .build();
    }

    @GivenTextResource("sqs-event-notification.json")
    String notificationEvent;

    @BeforeEach
    public void initData() {
        SQS sqs = new SQS();
        sqs.create(sqsQueueName());
        SQS.Response response = sqs.queueUrl(sqsQueueName());
        sqs.put(sqs.queueUrl(response), notificationEvent);
    }

    @Test
    void invoke(
            @GivenJsonResource("eventbridge-scheduled.json")
            AWSEvent event
    ) {
        Assertions.assertNotNull(event);

        FrequentPutEventBoundaryHandler handler = new FrequentPutEventBoundaryHandler();

        FrequentPutEventBoundaryObserver eventContext = mock();
        OffsetDateTime utc = OffsetDateTime.now(ZoneId.of("UTC")).plus(2, IntervalUnit.TWELFTHS);
        event.setTime(utc);
        handler.handleEvent(event, context(), eventContext);
        String testLot = new TestLots().format(utc.minus(1, IntervalUnit.TWELFTHS));

        verify(eventContext).applyDatasetItemsSize(1);
        verify(eventContext).applyLotId(testLot);
        verify(eventContext).applyIdentifierURI(URI.create("s3://DOC-EXAMPLE-BUCKET1/project/version/y=2023/m=12/d=31/data.json"));
        verify(eventContext).applyManifestURI(datasets().manifestPathList(ManifestState.complete).get(0).withLot(testLot).uri());
    }
}
