package clusterless.lambda.workload.s3copy;

import clusterless.lambda.BaseHandlerTest;
import clusterless.lambda.arc.ArcProps;
import clusterless.model.Struct;
import clusterless.model.deploy.Dataset;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * @ParameterizeTest with @MethodSource does not work because events() is not static.
 * And the BaseHandlerTest will fail if @TestInstance(PER_CLASS) is set to overcome that.
 */
public class S3CopyArcEventHandlerTest extends BaseHandlerTest {
    @Override
    protected Struct getProps() {
        return ArcProps.Builder.builder()
                .build();
    }

    Stream<ArcNotifyEvent> events() {
        Dataset dataset = getDataset();
        return Stream.of(ArcNotifyEvent.Builder.builder()
                .withDatasetName(dataset.name())
                .withDatasetVersion(dataset.version())
                .withManifestURI(getManifestURI())
                .withLotId("20230227PT5M287")
                .withDatasetPrefix(dataset.locationURI())
                .build());
    }

    public void invoke(
            ArcNotifyEvent event
    ) {
        Assertions.assertNotNull(event);

        S3CopyArcEventHandler handler = new S3CopyArcEventHandler();

        handler.handleRequest(event, context());
    }

    @TestFactory
    Stream<DynamicTest> tests() {
        return events().map(e -> dynamicTest(e.datasetId(), () -> invoke(e)));
    }
}
