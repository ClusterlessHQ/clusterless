package clusterless.lambda.arc;

import clusterless.model.Struct;
import clusterless.model.state.ArcState;
import clusterless.substrate.aws.event.ArcNotifyEvent;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArcExecContext implements Struct {
    ArcState previousState;
    ArcState currentState;
    String role;
    ArcNotifyEvent arcNotifyEvent;
    Map<String, URI> sinkCompleteManifest = new LinkedHashMap<>();
    Map<String, URI> sinkPartialManifest = new LinkedHashMap<>();
    Map<String, URI> sinkRemovedManifest = new LinkedHashMap<>();
    Map<String, List<URI>> existingPartialManifests = new LinkedHashMap<>();

    public ArcExecContext() {
    }

    public static Builder builder() {
        return Builder.anArcExecContext();
    }

    public ArcState previousState() {
        return previousState;
    }

    public ArcState currentState() {
        return currentState;
    }

    public String role() {
        return role;
    }

    public ArcNotifyEvent arcNotifyEvent() {
        return arcNotifyEvent;
    }

    public Map<String, URI> sinkCompleteManifest() {
        return sinkCompleteManifest;
    }

    public Map<String, URI> sinkPartialManifest() {
        return sinkPartialManifest;
    }

    public Map<String, URI> sinkRemovedManifest() {
        return sinkRemovedManifest;
    }

    public Map<String, List<URI>> existingPartialManifests() {
        return existingPartialManifests;
    }

    public static final class Builder {
        ArcState previousState;
        ArcState currentState;
        String role;
        ArcNotifyEvent arcNotifyEvent;
        Map<String, URI> sinkCompleteManifest = new LinkedHashMap<>();
        Map<String, URI> sinkPartialManifest = new LinkedHashMap<>();
        Map<String, URI> sinkRemovedManifest = new LinkedHashMap<>();
        Map<String, List<URI>> existingPartialManifests = new LinkedHashMap<>();

        private Builder() {
        }

        public static Builder anArcExecContext() {
            return new Builder();
        }

        public Builder withPreviousState(ArcState previousState) {
            this.previousState = previousState;
            return this;
        }

        public Builder withCurrentState(ArcState currentState) {
            this.currentState = currentState;
            return this;
        }

        public Builder withRole(String role) {
            this.role = role;
            return this;
        }

        public Builder withArcNotifyEvent(ArcNotifyEvent arcNotifyEvent) {
            this.arcNotifyEvent = arcNotifyEvent;
            return this;
        }

        public Builder withSinkCompleteManifest(Map<String, URI> sinkCompleteManifest) {
            this.sinkCompleteManifest = sinkCompleteManifest;
            return this;
        }

        public Builder withSinkPartialManifest(Map<String, URI> sinkPartialManifest) {
            this.sinkPartialManifest = sinkPartialManifest;
            return this;
        }

        public Builder withSinkRemovedManifest(Map<String, URI> sinkRemovedManifest) {
            this.sinkRemovedManifest = sinkRemovedManifest;
            return this;
        }

        public Builder withExistingPartialManifests(Map<String, List<URI>> existingPartialManifests) {
            this.existingPartialManifests = existingPartialManifests;
            return this;
        }

        public ArcExecContext build() {
            ArcExecContext arcExecContext = new ArcExecContext();
            arcExecContext.role = this.role;
            arcExecContext.sinkRemovedManifest = this.sinkRemovedManifest;
            arcExecContext.previousState = this.previousState;
            arcExecContext.currentState = this.currentState;
            arcExecContext.sinkPartialManifest = this.sinkPartialManifest;
            arcExecContext.existingPartialManifests = this.existingPartialManifests;
            arcExecContext.sinkCompleteManifest = this.sinkCompleteManifest;
            arcExecContext.arcNotifyEvent = this.arcNotifyEvent;
            return arcExecContext;
        }
    }
}
