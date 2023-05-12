package clusterless.substrate.aws.event;

import clusterless.model.Struct;
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
    ArcExecContext arcExecContext = new ArcExecContext();
    Map<String, URI> sinkManifests = new LinkedHashMap<>(); // result of the arc workload

    public ArcStateContext() {
    }

    public static Builder builder() {
        return Builder.anArcStateContext();
    }

    public ArcState previousState() {
        return previousState;
    }

    public ArcState currentState() {
        return currentState;
    }

    public String role() {
        return arcExecContext.role();
    }

    public ArcNotifyEvent arcNotifyEvent() {
        return arcExecContext.arcNotifyEvent();
    }

    public Map<String, List<URI>> existingPartialManifests() {
        return arcExecContext.existingPartialManifests();
    }

    public Map<String, URI> sinkManifests() {
        return sinkManifests;
    }

    public static final class Builder {
        ArcState previousState;
        ArcState currentState;
        ArcExecContext arcExecContext = new ArcExecContext();
        Map<String, URI> sinkManifests = new LinkedHashMap<>(); // result of the arc workload

        private Builder() {
        }

        public static Builder anArcStateContext() {
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

        public Builder withArcExecContext(ArcExecContext arcExecContext) {
            this.arcExecContext = arcExecContext;
            return this;
        }

        public Builder withSinkManifests(Map<String, URI> sinkManifests) {
            this.sinkManifests = sinkManifests;
            return this;
        }

        public ArcStateContext build() {
            ArcStateContext arcStateContext = new ArcStateContext();
            arcStateContext.sinkManifests = this.sinkManifests;
            arcStateContext.arcExecContext = this.arcExecContext;
            arcStateContext.previousState = this.previousState;
            arcStateContext.currentState = this.currentState;
            return arcStateContext;
        }
    }
}
