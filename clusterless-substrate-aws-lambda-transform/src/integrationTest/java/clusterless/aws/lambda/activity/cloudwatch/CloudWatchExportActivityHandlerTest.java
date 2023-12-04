/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.activity.cloudwatch;

import clusterless.aws.lambda.LocalStackBase;
import clusterless.aws.lambda.TestLots;
import clusterless.aws.lambda.transform.json.event.AWSEvent;
import clusterless.cls.json.JSONUtil;
import clusterless.commons.temporal.IntervalUnit;
import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.adelean.inject.resources.junit.jupiter.WithJacksonMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.mockito.Mockito.*;

@TestWithResources
public class CloudWatchExportActivityHandlerTest extends LocalStackBase {
    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchExportActivityHandlerTest.class);
    @WithJacksonMapper
    static ObjectMapper objectMapper = JSONUtil.OBJECT_MAPPER;

    @Override
    protected CloudWatchExportActivityProps getProps() {
        return CloudWatchExportActivityProps.builder()
                .withLogGroupName("test-log-group")
                .withDestinationURI(URI.create("s3://%s/test-prefix/".formatted(bucketName())))
                .withInterval(IntervalUnit.TWELFTHS.name())
                .build();
    }

    @Test
    void invoke(
            @GivenJsonResource("eventbridge-scheduled.json")
            AWSEvent event
    ) {
        Assertions.assertNotNull(event);

        CloudWatchExportActivityHandler handler = new CloudWatchExportActivityHandler();

        CloudWatchExportActivityObserver eventContext = mock();

        when(eventContext.enableExport()).thenReturn(false);

        OffsetDateTime utc = OffsetDateTime.now(ZoneId.of("UTC")).plus(2, IntervalUnit.TWELFTHS);
        event.setTime(utc);
        handler.handleEvent(event, context(), eventContext);
        String testLot = new TestLots().format(utc.minus(1, IntervalUnit.TWELFTHS));

        OffsetDateTime end = utc.truncatedTo(IntervalUnit.TWELFTHS);
        OffsetDateTime begin = end.minus(1, IntervalUnit.TWELFTHS);

        verify(eventContext).applyInterval(testLot);
        Instant beginInclusive = begin.toInstant();
        Instant endInclusive = end.minus(1, ChronoUnit.MILLIS).toInstant();

        LOG.info("event: {}", utc);
        LOG.info("begin: {}", beginInclusive);
        LOG.info("end: {}", endInclusive);

        verify(eventContext).applyRange(beginInclusive, endInclusive);
    }
}
