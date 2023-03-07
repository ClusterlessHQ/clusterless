package clusterless.lambda.arc;

import clusterless.lambda.EventObserver;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.manifest.Manifest;

public interface ArcEventObserver extends EventObserver {
    default void applyManifest(Manifest manifest) {
    }

    default void applyToDataset(String role, SinkDataset sinkDataset) {

    }
}
