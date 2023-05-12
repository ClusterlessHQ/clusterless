package clusterless.substrate.aws.event;

import clusterless.model.Struct;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArcExecContext implements Struct {
    String role;
    ArcNotifyEvent arcNotifyEvent;
    Map<String, List<URI>> existingPartialManifests = new LinkedHashMap<String, List<URI>>();

    public ArcExecContext() {
    }

    public static Builder builder() {
        return Builder.anArcExecContext();
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

    public static final class Builder {
        String role;
        ArcNotifyEvent arcNotifyEvent;
        Map<String, List<URI>> existingPartialManifests = new LinkedHashMap<String, List<URI>>();

        private Builder() {
        }

        public static Builder anArcExecContext() {
            return new Builder();
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

        public ArcExecContext build() {
            ArcExecContext arcExecContext = new ArcExecContext();
            arcExecContext.arcNotifyEvent = this.arcNotifyEvent;
            arcExecContext.role = this.role;
            arcExecContext.existingPartialManifests = this.existingPartialManifests;
            return arcExecContext;
        }
    }
}
