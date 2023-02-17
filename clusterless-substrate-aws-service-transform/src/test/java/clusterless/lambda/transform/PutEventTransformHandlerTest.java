/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.transform;

import clusterless.json.JSONUtil;
import clusterless.lambda.manifest.ManifestRequest;
import clusterless.lambda.transform.json.AWSEvent;
import clusterless.substrate.aws.sdk.EventBus;
import clusterless.substrate.aws.sdk.S3;
import clusterless.temporal.IntervalUnit;
import clusterless.util.Env;
import clusterless.util.URIs;
import com.adelean.inject.resources.junit.jupiter.GivenJsonResource;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.adelean.inject.resources.junit.jupiter.WithJacksonMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.net.URI;

/**
 *
 */
@Testcontainers
@TestWithResources
@ExtendWith(SystemStubsExtension.class)
public class PutEventTransformHandlerTest {
    public static final String EVENT_BUS = "forwarding-bus";
    public static final String DATASET_NAME = "dataset";
    public static final String DATASET_VERSION = "20230101";
    public static final URI MANIFESTS = URIs.copyAppendPath(URI.create("s3://manifests/"), DATASET_NAME, DATASET_VERSION);
    static DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:1.3.1");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(localstackImage)
            .withServices(
                    LocalStackContainer.Service.S3,
                    LocalStackContainer.EnabledService.named("events")
            );

    @WithJacksonMapper
    static ObjectMapper objectMapper = JSONUtil.OBJECT_MAPPER;
    TransformProps transformProps = TransformProps.Builder.builder()
            .withLotSource(LotSource.eventTime)
            .withManifestPrefix(MANIFESTS)
            .withLotUnit(IntervalUnit.TWELFTHS.name())
            .withDatasetName(DATASET_NAME)
            .withDatasetVersion(DATASET_VERSION)
            .withEventBusName(EVENT_BUS)
            .build();
    @SystemStub
    private EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(Env.key(transformProps), Env.value(transformProps))
            .set("AWS_ACCESS_KEY_ID", localstack.getAccessKey())
            .set("AWS_SECRET_ACCESS_KEY", localstack.getSecretKey())
            .set("AWS_DEFAULT_REGION", localstack.getRegion())
            .set("AWS_S3_ENDPOINT", localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
    Context context;

    @BeforeEach
    void setUp() {
        S3 s3 = new S3();
        S3.Response s3Response = s3.create(MANIFESTS.getHost());
        if (!s3Response.isSuccess()) {
            throw new RuntimeException(s3Response.exception());
        }

        EventBus eventBus = new EventBus();
        EventBus.Response eventResponse = eventBus.create(EVENT_BUS);
        if (!eventResponse.isSuccess()) {
            throw new RuntimeException(eventResponse.exception());
        }
    }

    @Test
    void invoke(
            @GivenJsonResource("eventbridge-object-created.json")
            AWSEvent event
    ) {
        Assertions.assertNotNull(event);

        PutEventTransformHandler handler = new PutEventTransformHandler();

        ManifestRequest request = new ManifestRequest();

        handler.handleEvent(event, context, request);

        String lotId = "20211112PT5M000";
        Assertions.assertEquals(lotId, request.lotId());
        Assertions.assertEquals(1, request.datasetItemsSize());
        Assertions.assertEquals(URIs.copyAppendPath(MANIFESTS, "lot=" + lotId, "manifest.json"), request.manifestURI());
    }
}
