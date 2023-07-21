/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.workload.batch;

import clusterless.lambda.CreateDataMachine;
import clusterless.lambda.LocalStackBase;
import clusterless.lambda.TestDatasets;
import clusterless.lambda.arc.ArcEventObserver;
import clusterless.lambda.arc.ArcProps;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.deploy.WorkloadProps;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.substrate.aws.event.ArcWorkloadContext;
import clusterless.substrate.uri.ManifestURI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.net.URI;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @ParameterizeTest with @MethodSource does not work because events() is not static.
 * And the BaseHandlerTest will fail if @TestInstance(PER_CLASS) is set to overcome that.
 */
public class BatchArcEventHandlerTest extends LocalStackBase {
    public static final String SUCCESS_LOT = "20230227PT5M287";
    public static final String PARTIAL_LOT = "20230227PT5M288";
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
                .withSources(datasets().sourceDatasetMap())
                .withSourceManifestPaths(datasets().sourceManifestPathMap())
                .withSinks(datasets().sinkDatasetMap())
                .withSinkManifestTemplates(datasets().sinkManifestPathMap())
                .withWorkloadProps(new WorkloadProps())
                .build();
    }

    Stream<ArcWorkloadContext> events() {
        return Stream.of(
                ArcWorkloadContext.builder()
                        .withArcNotifyEvent(
                                ArcNotifyEvent.Builder.builder()
                                        .withDataset(datasets().sourceDatasetMap().get("main"))
                                        .withManifest(datasets().manifestIdentifierMap(SUCCESS_LOT, datasets().sourceDatasetMap(), ManifestState.complete).get("main").uri())
                                        .withLot(SUCCESS_LOT)
                                        .build()
                        )
                        .withRole("main")
                        .build()
        );
    }

    @BeforeEach
    void initData() {
        ArcProps<?> props = getProps();

        new CreateDataMachine(SUCCESS_LOT)
                .applyBucketsFrom(props.sources())
                .applyBucketsFrom(props.sinks())
                .buildSources(props.sourceManifestPaths(), props.sources())
                .buildSinks(props.sinkManifestTemplates(), props.sinks(), ManifestState.complete);

//        new CreateDataMachine(PARTIAL_LOT)
//                .applyBucketsFrom(props.sources())
//                .applyBucketsFrom(props.sinks())
//                .buildSources(props.sourceManifestPaths(), props.sources())
//                .buildSinks(props.sinkManifestPaths(), props.sinks(), ManifestState.partial);
    }

    public void invoke(
            ArcWorkloadContext arcWorkloadContext
    ) {
        Assertions.assertNotNull(arcWorkloadContext);

        BatchResultHandler handler = new BatchResultHandler();

        ArcEventObserver eventObserver = mock();

        Map<String, URI> result = handler.handleEvent(arcWorkloadContext, context(), eventObserver);

        Assertions.assertFalse(result.isEmpty());

        SinkDataset mainSink = getProps().sinks().get("main");
        verify(eventObserver).applyToManifest(argThat(a -> a.equals("main")), argThat(u -> ManifestURI.parse(u).dataset().name().equals(mainSink.name())));
        verify(eventObserver).applyToManifest(argThat(a -> a.equals("main")), argThat(u -> ManifestURI.parse(u).dataset().version().equals(mainSink.version())));
    }

    @TestFactory
    Stream<DynamicTest> tests() {
        return events().map(e -> dynamicTest(e.arcNotifyEvent().datasetId(), () -> invoke(e)));
    }
}
