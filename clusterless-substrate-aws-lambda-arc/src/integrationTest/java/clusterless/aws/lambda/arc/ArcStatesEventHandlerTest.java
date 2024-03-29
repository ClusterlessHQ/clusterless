/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.arc;

import clusterless.aws.lambda.CreateDataMachine;
import clusterless.aws.lambda.LocalStackBase;
import clusterless.aws.lambda.TestDatasets;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.model.state.ArcState;
import clusterless.cls.substrate.aws.event.ArcNotifyEvent;
import clusterless.cls.substrate.aws.event.ArcStateContext;
import clusterless.cls.substrate.uri.ArcStateURI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @ParameterizeTest with @MethodSource does not work because events() is not static.
 * And the BaseHandlerTest will fail if @TestInstance(PER_CLASS) is set to overcome that.
 */
public class ArcStatesEventHandlerTest extends LocalStackBase {
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

    Stream<ArcNotifyEvent> events() {
        return Stream.of(
                ArcNotifyEvent.Builder.builder()
                        .withDataset(datasets().sourceDatasetMap().get("main"))
                        .withManifest(datasets().manifestIdentifierMap("20230227PT5M287", datasets().datasetMap(), ManifestState.complete).get("main").uri())
                        .withLot("20230227PT5M287")
                        .build()
        );
    }

    @BeforeEach
    void initData() {
        new CreateDataMachine("20230227PT5M287")
                .applyBucketsFrom(datasets().sourceDatasetMap())
                .applyBucketsFrom(datasets().sinkDatasetMap())
                .buildSources(datasets().manifestPathMapFor(datasets().sourceDatasetMap()), datasets().sourceDatasetMap());
    }

    public void invoke(
            ArcNotifyEvent arcNotifyEvent
    ) {
        Assertions.assertNotNull(arcNotifyEvent);

        // enter the state machine
        ArcStateStartObserver startObserver = mock();
        ArcStateStartHandler startHandler = new ArcStateStartHandler();
        ArcStateContext startContext = startHandler.handleEvent(arcNotifyEvent, context(), startObserver);

        Assertions.assertEquals(ArcState.running, startContext.currentState());
        Assertions.assertEquals(arcNotifyEvent, startContext.arcWorkloadContext().arcNotifyEvent());

        verify(startObserver).applyRoles(List.of("main"));
        verify(startObserver).applyFinalArcStates(null, ArcState.running);

        // insert the workload results
        startContext.sinkManifests().put("main", datasets().manifestIdentifierMap("20230227PT5M287", datasets().sinkDatasetMap(), ManifestState.complete).get("main").uri());

        // handle the results of the workload
        ArcStateCompleteObserver completeObserver = mock();
        ArcStateCompleteHandler completeHandler = new ArcStateCompleteHandler();
        ArcStateContext completeContext = completeHandler.handleEvent(startContext, context(), completeObserver);

        Assertions.assertEquals(ArcState.complete, completeContext.currentState());
        Assertions.assertEquals(arcNotifyEvent.toString(), completeContext.arcWorkloadContext().arcNotifyEvent().toString());
    }

    @TestFactory
    Stream<DynamicTest> tests() {
        return events().map(e -> dynamicTest(e.datasetId(), () -> invoke(e)));
    }
}
