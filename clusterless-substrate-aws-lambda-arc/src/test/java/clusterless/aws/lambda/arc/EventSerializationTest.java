/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.arc;

import clusterless.aws.lambda.LambdaHandlerTestBase;
import clusterless.aws.lambda.TestDatasets;
import clusterless.cls.json.JSONUtil;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.model.state.ArcState;
import clusterless.cls.substrate.aws.event.ArcNotifyEvent;
import clusterless.cls.substrate.aws.event.ArcStateContext;
import clusterless.cls.substrate.uri.ArcStateURI;
import clusterless.cls.util.Env;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import software.amazon.awssdk.utils.StringInputStream;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SystemStubsExtension.class)
public class EventSerializationTest extends LambdaHandlerTestBase {
    @SystemStub
    private EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(Env.keyTyped(getProps()), Env.valueTyped(getProps())); // shove the props json into an env var

    TestDatasets datasets;

    public TestDatasets datasets() {
        if (datasets == null) {
            datasets = new TestDatasets(defaultPlacement(), "main");
        }

        return datasets;
    }

    @Override
    protected ArcStateProps getProps() {
        return ArcStateProps.builder()
                .withName("test-arc")
                .withProject(defaultProject())
                .withSinks(datasets().sinkDatasetMapAsSink())
                .withSources(datasets().sourceDatasetMapAsSource())
                .withArcStatePath(
                        ArcStateURI.builder()
                                .withPlacement(defaultPlacement())
                                .withProject(defaultProject())
                                .withArcName("test-arc")
                                .build()
                )
                .withEventBusName(eventBusName())
                .build();
    }

    @Test
    void handlers() throws IOException {
        String lotId = "20230227PT5M287";
        ArcNotifyEvent arcNotifyEvent = ArcNotifyEvent.Builder.builder()
                .withDataset(datasets().sourceDatasetMap().get("main"))
                .withManifest(datasets().manifestIdentifierMap(lotId, datasets().sourceDatasetMap(), ManifestState.complete).get("main").uri())
                .withLot(lotId)
                .build();

        InputStream inputStream = new StringInputStream(JSONUtil.writeAsStringSafe(arcNotifyEvent));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ArcStateManager arcStateManager = mock();

        new ArcStateStartHandler(arcStateManager).handleRequest(inputStream, outputStream, null);

        ArcStateContext startContext = JSONUtil.readObjectSafe(outputStream.toByteArray(), ArcStateContext.class);

        Assertions.assertNotNull(startContext);
        Assertions.assertEquals(arcNotifyEvent.toString(), startContext.arcWorkloadContext().arcNotifyEvent().toString());

        startContext.sinkManifests().put("main", datasets().manifestIdentifierMap(lotId, datasets().sinkDatasetMap(), ManifestState.complete).get("main").uri());

        inputStream = new StringInputStream(JSONUtil.writeAsStringSafe(startContext));
        outputStream = new ByteArrayOutputStream();

        arcStateManager = mock();
        when(arcStateManager.setStateFor(anyString(), eq(ArcState.complete))).thenReturn(Optional.of(ArcState.running));
        when(arcStateManager.setStateFor(anyString(), eq(ArcState.complete), any())).thenReturn(Optional.of(ArcState.running));

        Map<String, ArcNotifyEventPublisher> eventPublishers = Map.of("main", Mockito.mock(ArcNotifyEventPublisher.class));

        new ArcStateCompleteHandler(arcStateManager, eventPublishers).handleRequest(inputStream, outputStream, null);

        ArcStateContext completeContext = JSONUtil.readObjectSafe(outputStream.toByteArray(), ArcStateContext.class);

        Assertions.assertNotNull(completeContext);
        Assertions.assertEquals(arcNotifyEvent.toString(), completeContext.arcWorkloadContext().arcNotifyEvent().toString());
    }
}
