/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.workload.s3copy;

import clusterless.lambda.CreateDataMachine;
import clusterless.lambda.LocalStackBase;
import clusterless.lambda.TestDatasets;
import clusterless.lambda.arc.ArcEventObserver;
import clusterless.lambda.arc.ArcProps;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.substrate.aws.event.ArcWorkloadContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.net.URI;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @ParameterizeTest with @MethodSource does not work because events() is not static.
 * And the BaseHandlerTest will fail if @TestInstance(PER_CLASS) is set to overcome that.
 */
public class S3CopyArcEventHandlerTest extends LocalStackBase {
    TestDatasets datasets;

    public TestDatasets datasets() {
        if (datasets == null) {
            datasets = new TestDatasets(defaultPlacement(), "main");
        }

        return datasets;
    }

    @Override
    protected ArcProps<?> getProps() {
        return ArcProps.builder()
                .withSources(datasets().sourceDatasetMapAsSource())
                .withSourceManifestPaths(datasets().sourceManifestPathMap())
                .withSinks(datasets().sinkDatasetMapAsSink())
                .withSinkManifestTemplates(datasets().sinkManifestPathMap())
                .withWorkloadProps(new S3CopyProps())
                .build();
    }

    Stream<ArcWorkloadContext> events() {
        return Stream.of(
                ArcWorkloadContext.builder()
                        .withArcNotifyEvent(
                                ArcNotifyEvent.Builder.builder()
                                        .withDataset(datasets().sourceDatasetMap().get("main"))
                                        .withManifest(datasets().manifestIdentifierMap("20230227PT5M287", datasets().sourceDatasetMap(), ManifestState.complete).get("main").uri())
                                        .withLot("20230227PT5M287")
                                        .build()
                        )
                        .withRole("main")
                        .build()
        );
    }

    @BeforeEach
    void initData() {
        ArcProps<?> props = getProps();

        new CreateDataMachine("20230227PT5M287")
                .applyBucketsFrom(datasets().sourceDatasetMap())
                .applyBucketsFrom(datasets().sinkDatasetMap())
                .buildSources(props.sourceManifestPaths(), datasets().sourceDatasetMap());
    }

    public void invoke(
            ArcWorkloadContext arcWorkloadContext
    ) {
        Assertions.assertNotNull(arcWorkloadContext);

        S3CopyArcEventHandler handler = new S3CopyArcEventHandler();

        ArcEventObserver eventObserver = mock();

        Map<String, URI> result = handler.handleEvent(arcWorkloadContext, context(), eventObserver);

        Assertions.assertFalse(result.isEmpty());

        ArcNotifyEvent arcNotifyEvent = arcWorkloadContext.arcNotifyEvent();
        verify(eventObserver).applyFromManifest(argThat(u -> u.equals(arcNotifyEvent.manifest())), argThat(m -> m.uris().size() == 1));
        verify(eventObserver).applyFromManifest(argThat(u -> u.equals(arcNotifyEvent.manifest())), isNotNull());
        verify(eventObserver).applyFromManifest(argThat(u -> u.equals(arcNotifyEvent.manifest())), isNotNull());

        SinkDataset mainSink = getProps().sinks().get("main");
        verify(eventObserver).applyToDataset(argThat(s -> s.equals("main")), argThat(d -> d.name().equals(mainSink.name())));
    }

    @TestFactory
    Stream<DynamicTest> tests() {
        return events().map(e -> dynamicTest(e.arcNotifyEvent().datasetId(), () -> invoke(e)));
    }
}
