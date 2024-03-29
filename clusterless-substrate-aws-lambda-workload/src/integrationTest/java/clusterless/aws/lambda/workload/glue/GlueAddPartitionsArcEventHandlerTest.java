/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.workload.glue;

import clusterless.aws.lambda.CreateDataMachine;
import clusterless.aws.lambda.LocalStackBase;
import clusterless.aws.lambda.TestDatasets;
import clusterless.aws.lambda.arc.ArcEventObserver;
import clusterless.aws.lambda.arc.ArcProps;
import clusterless.cls.model.deploy.SinkDataset;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.substrate.aws.event.ArcNotifyEvent;
import clusterless.cls.substrate.aws.event.ArcWorkloadContext;
import clusterless.cls.substrate.aws.sdk.Glue;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import software.amazon.awssdk.services.glue.model.Table;

import java.net.URI;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.Mockito.*;

/**
 * @ParameterizeTest with @MethodSource does not work because events() is not static.
 * And the BaseHandlerTest will fail if @TestInstance(PER_CLASS) is set to overcome that.
 */
public class GlueAddPartitionsArcEventHandlerTest extends LocalStackBase {
    TestDatasets datasets;

    @Override
    protected boolean usesGlue() {
        return false;
    }

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
                .withSinks(
                        Map.of(
                                "main",
                                SinkDataset.Builder.builder()
                                        .withPathURI(URI.create("glue:///%s/%s".formatted(glueDatabaseName(), glueTableName())))
                                        .withName("main")
                                        .withVersion("1")
                                        .build()
                        ))
                .withSinkManifestTemplates(datasets().sinkManifestPathMap())
                .withWorkloadProps(new GlueAddPartitionsProps())
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

        GlueAddPartitionsArcEventHandler.glue = mockGlue();

        GlueAddPartitionsArcEventHandler handler = new GlueAddPartitionsArcEventHandler();

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

    @NotNull
    private Glue mockGlue() {
        Glue glue = mock();
        Glue.Response tableResponse = mock();
        Glue.Response partitionResponse = mock();

        when(glue.getTable(anyString(), anyString())).thenReturn(tableResponse);
        Table table = Table.builder()
                .databaseName(glueDatabaseName())
                .name(glueTableName())
                .build();
        when(glue.getTable(any())).thenReturn(table);
        when(glue.addPartitions(nullable(String.class), any(Table.class), anyMap())).thenReturn(partitionResponse);
        when(glue.hasBatchErrors(any())).thenReturn(false);

        return glue;
    }

    @TestFactory
    Stream<DynamicTest> tests() {
        return events().map(e -> dynamicTest(e.arcNotifyEvent().datasetId(), () -> invoke(e)));
    }
}
