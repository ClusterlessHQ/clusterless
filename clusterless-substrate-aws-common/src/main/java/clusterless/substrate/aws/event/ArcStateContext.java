package clusterless.substrate.aws.event;

import clusterless.model.Struct;
import clusterless.model.manifest.ManifestState;
import clusterless.model.state.ArcState;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The ArcExecContext is the object passed within a state machine.
 */
public class ArcStateContext implements Struct {
    ArcState previousState;
    ArcState currentState;
    String role;
    ArcNotifyEvent arcNotifyEvent;
    Map<String, List<URI>> existingPartialManifests = new LinkedHashMap<>();
    Map<String, ManifestState> sinkStates = new LinkedHashMap<>(); // result of the arc workload

    public ArcStateContext() {
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

    public Map<String, List<URI>> existingPartialManifests() {
        return existingPartialManifests;
    }

    public Map<String, ManifestState> sinkStates() {
        return sinkStates;
    }

    public static final class Builder {
        ArcState previousState;
        ArcState currentState;
        String role;
        ArcNotifyEvent arcNotifyEvent;
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

        public Builder withExistingPartialManifests(Map<String, List<URI>> existingPartialManifests) {
            this.existingPartialManifests = existingPartialManifests;
            return this;
        }

        public ArcStateContext build() {
            ArcStateContext arcStateContext = new ArcStateContext();
            arcStateContext.arcNotifyEvent = this.arcNotifyEvent;
            arcStateContext.previousState = this.previousState;
            arcStateContext.currentState = this.currentState;
            arcStateContext.role = this.role;
            arcStateContext.existingPartialManifests = this.existingPartialManifests;
            return arcStateContext;
        }
    }
}
