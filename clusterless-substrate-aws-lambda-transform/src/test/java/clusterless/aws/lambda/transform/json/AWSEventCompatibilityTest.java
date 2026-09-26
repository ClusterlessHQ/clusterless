/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.transform.json;

import clusterless.aws.lambda.StreamHandler;
import clusterless.aws.lambda.transform.json.object.AWSEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * AWS owns the EventBridge event schemas and adds fields to them without notice; the
 * S3 "Object Created" detail gained {@code event-version}, which failed every event
 * delivered to the infrequent put listener. The generated models must read events
 * carrying fields they do not declare.
 */
public class AWSEventCompatibilityTest {
    @Test
    void objectCreatedToleratesFieldsAddedByAws() throws IOException {
        try (InputStream input = getClass().getResourceAsStream("/eventbridge-object-created-current.json")) {
            AWSEvent event = StreamHandler.objectReaderFor(AWSEvent.class).readValue(input);

            Assertions.assertEquals("DOC-EXAMPLE-BUCKET1", event.getDetail().getBucket().getName());
            Assertions.assertEquals("project/version/y=2023/m=12/d=31/data.json", event.getDetail().getObject().getKey());
        }
    }
}
