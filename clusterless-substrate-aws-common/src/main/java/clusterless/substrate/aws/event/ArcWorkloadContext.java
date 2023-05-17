/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.event;

import clusterless.model.Struct;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ArcWorkloadContext implements Struct {
    String role;
    ArcNotifyEvent arcNotifyEvent;
    Map<String, List<URI>> existingPartialManifests = new LinkedHashMap<>();

    public ArcWorkloadContext() {
    }

    public static Builder builder() {
        return Builder.builder();
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
        Map<String, List<URI>> existingPartialManifests = new LinkedHashMap<>();

        private Builder() {
        }

        public static Builder builder() {
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

        public ArcWorkloadContext build() {
            ArcWorkloadContext arcWorkloadContext = new ArcWorkloadContext();
            arcWorkloadContext.arcNotifyEvent = this.arcNotifyEvent;
            arcWorkloadContext.existingPartialManifests = this.existingPartialManifests;
            arcWorkloadContext.role = this.role;
            return arcWorkloadContext;
        }
    }
}
