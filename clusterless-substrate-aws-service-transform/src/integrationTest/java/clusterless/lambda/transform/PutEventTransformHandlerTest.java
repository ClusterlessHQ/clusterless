/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.lambda.BaseHandlerTest;
import clusterless.lambda.manifest.ManifestEventContext;
import clusterless.lambda.transform.json.AWSEvent;
import clusterless.temporal.IntervalUnit;
import clusterless.util.URIs;
import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class PutEventTransformHandlerTest extends BaseHandlerTest {
    @Override
    protected TransformProps getProps() {
        return TransformProps.Builder.builder()
                .withLotSource(LotSource.eventTime)
                .withManifestPrefix(getManifestURI())
                .withLotUnit(IntervalUnit.TWELFTHS.name())
                .withDatasetName(getDataset().name())
                .withDatasetVersion(getDataset().version())
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

        ManifestEventContext eventContext = new ManifestEventContext();

        handler.handleEvent(event, context(), eventContext);

        String lotId = "20211112PT5M000";
        Assertions.assertEquals(lotId, eventContext.lotId());
        Assertions.assertEquals(1, eventContext.datasetItemsSize());
        Assertions.assertEquals(URIs.copyAppendPath(getManifestURI(), "lot=" + lotId, "manifest.json"), eventContext.manifestURI());
    }
}
