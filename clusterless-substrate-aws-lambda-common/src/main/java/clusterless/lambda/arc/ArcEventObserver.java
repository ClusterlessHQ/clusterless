package clusterless.lambda.arc;

import clusterless.lambda.EventObserver;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.manifest.Manifest;

import java.net.URI;

public interface ArcEventObserver extends EventObserver {
    default void applyFromManifest(Manifest manifest) {
    }

    default void applyToDataset(String role, SinkDataset sinkDataset) {

    }

    default void applyToManifest(String role, URI manifest) {

    }
}
