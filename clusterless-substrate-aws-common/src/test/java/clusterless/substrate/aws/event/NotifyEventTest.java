/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.event;

import clusterless.json.JSONUtil;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

/**
 *
 */
public class NotifyEventTest {
    @Test
    void arcNotifyEvent() throws IOException {
        ArcNotifyEvent event = ArcNotifyEvent.Builder.builder()
                .withDatasetName("name")
                .withDatasetVersion("version")
                .withLotId("20220101")
                .withManifestURI(URI.create("s3://foo/bar"))
                .build();

        String json = JSONUtil.writeAsStringSafe(event);

        JsonNode jsonNode = JSONUtil.readTree(json);

        JsonNode result = jsonNode.at("/datasetId");
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isNull());
        Assertions.assertEquals("name/version", result.asText());
    }
}
