package clusterless.lambda.arc;

import clusterless.lambda.EventObserver;

import java.net.URI;
import java.util.Map;

public interface ArcStateCompleteObserver extends EventObserver {
    void applySinkManifests(Map<String, URI> sinkStates);
}
