package clusterless.lambda.workload.s3copy;

import clusterless.lambda.CreateDataMachine;
import clusterless.lambda.LocalStackBase;
import clusterless.lambda.TestDatasets;
import clusterless.lambda.arc.ArcEventObserver;
import clusterless.lambda.arc.ArcProps;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.deploy.SourceDataset;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @ParameterizeTest with @MethodSource does not work because events() is not static.
 * And the BaseHandlerTest will fail if @TestInstance(PER_CLASS) is set to overcome that.
 */
public class S3CopyArcEventHandlerTest extends LocalStackBase {

    static TestDatasets datasets = new TestDatasets("main");

    @Override
    protected ArcProps getProps() {
        return ArcProps.Builder.builder()
                .withSources(datasets.sourceDatasetMap())
                .withSourceManifestPaths(datasets.sourceManifestPathMap())
                .withSinks(datasets.sinkDatasetMap())
                .withSinkManifestPaths(datasets.sinkManifestPathMap())
                .build();
    }

    Stream<ArcNotifyEvent> events() {
        return Stream.of(
                ArcNotifyEvent.Builder.builder()
                        .withDataset(datasets.sourceDatasetMap().get("main"))
                        .withManifest(datasets.manifestIdentifierMap("20230227PT5M287", datasets.sourceDatasetMap()).get("main"))
                        .withLotId("20230227PT5M287")
                        .build()
        );
    }

    @BeforeEach
    void initData() {
        ArcProps props = getProps();

        new CreateDataMachine("20230227PT5M287")
                .applyBucketsFrom(props.sources())
                .applyBucketsFrom(props.sinks())
                .buildSources(props.sourceManifestPaths(), props.sources());
    }

    public void invoke(
            ArcNotifyEvent event
    ) {
        Assertions.assertNotNull(event);

        S3CopyArcEventHandler handler = new S3CopyArcEventHandler();

        ArcEventObserver eventContext = mock();

        handler.handleEvent(event, context(), eventContext);

        verify(eventContext).applyFromManifest(argThat(m -> m.uris().size() == 1));
        SourceDataset mainSource = getProps().sources().get("main");
        verify(eventContext).applyFromManifest(argThat(m -> m.dataset().name().equals(mainSource.name())));
        verify(eventContext).applyFromManifest(argThat(m -> m.dataset().version().equals(mainSource.version())));

        SinkDataset mainSink = getProps().sinks().get("main");
        verify(eventContext).applyToDataset(argThat(s -> s.equals("main")), argThat(d -> d.name().equals(mainSink.name())));
    }

    @TestFactory
    Stream<DynamicTest> tests() {
        return events().map(e -> dynamicTest(e.datasetId(), () -> invoke(e)));
    }
}
