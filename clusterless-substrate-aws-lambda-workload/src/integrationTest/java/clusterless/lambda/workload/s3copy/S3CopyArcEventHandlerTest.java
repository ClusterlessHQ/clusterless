package clusterless.lambda.workload.s3copy;

import clusterless.lambda.BaseHandlerTest;
import clusterless.lambda.TestDatasets;
import clusterless.lambda.arc.ArcEventObserver;
import clusterless.lambda.arc.ArcProps;
import clusterless.model.Struct;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.manifest.Manifest;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import org.junit.Ignore;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * @ParameterizeTest with @MethodSource does not work because events() is not static.
 * And the BaseHandlerTest will fail if @TestInstance(PER_CLASS) is set to overcome that.
 */
public class S3CopyArcEventHandlerTest extends BaseHandlerTest {

    static TestDatasets sourceDatasets = new TestDatasets("main");
    static TestDatasets sinkDatasets = new TestDatasets("main");

    @Override
    protected Struct getProps() {
        return ArcProps.Builder.builder()
                .withSources(sourceDatasets.sourceDatasetMap())
                .withSourceManifestPaths(sourceDatasets.manifestPathMap())
                .withSinks(sinkDatasets.sinkDatasetMap())
                .withSinkManifestPaths(sinkDatasets.manifestPathMap())
                .build();
    }

    Stream<ArcNotifyEvent> events() {
        return Stream.of(
                ArcNotifyEvent.Builder.builder()
                        .withDataset(sourceDatasets.sourceDatasetMap().get("main"))
                        .withManifest(sourceDatasets.manifestIdentifierMap("20230227PT5M287").get("main"))
                        .withLotId("20230227PT5M287")
                        .build()
        );
    }

    public void invoke(
            ArcNotifyEvent event
    ) {
        Assertions.assertNotNull(event);

        S3CopyArcEventHandler handler = new S3CopyArcEventHandler();

        ArcEventObserver eventContext = new ArcEventObserver() {
            @Override
            public void applyManifest(Manifest manifest) {

            }

            @Override
            public void applyToDataset(String role, SinkDataset sinkDataset) {

            }
        };

        handler.handleEvent(event, context(), eventContext);
    }

    //    @TestFactory
    @Ignore
    Stream<DynamicTest> tests() {
        return events().map(e -> dynamicTest(e.datasetId(), () -> invoke(e)));
    }
}
