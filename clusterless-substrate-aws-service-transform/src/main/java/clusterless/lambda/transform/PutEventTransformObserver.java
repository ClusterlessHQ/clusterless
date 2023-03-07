package clusterless.lambda.transform;

import clusterless.lambda.EventObserver;

import java.net.URI;
import java.time.OffsetDateTime;

public interface PutEventTransformObserver extends EventObserver {
    default void applyLotId(String lotId) {

    }

    default void applyDatasetItemsSize(int datasetItemsSize) {

    }

    default void applyManifestURI(URI manifestURI) {

    }

    default void applyEvent(OffsetDateTime time, String bucket, String key) {

    }

    default void applyIdentifierURI(URI identifierURI) {

    }
}
