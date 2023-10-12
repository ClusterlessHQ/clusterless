/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.event;

import clusterless.model.Struct;
import clusterless.model.deploy.LocatedDataset;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

/**
 * The ArcNotifyEvent is passed between Arc state machines.
 * <p>
 * This is the JSON observed on and subscribed to on an event bus.
 */
public class ArcNotifyEvent implements NotifyEvent, Struct {
    public static final String SOURCE = "clusterless.arc";
    public static final String DETAIL = "Clusterless Arc Notification";
    public static final String DATASET_ID = "datasetId";

    LocatedDataset dataset;
    String lot;
    URI manifest;

    public ArcNotifyEvent() {
    }

    private ArcNotifyEvent(Builder builder) {
        // dataset has subclasses with additional properties we don't need, so normalize
        dataset = builder.dataset == null ? null : new LocatedDataset(builder.dataset);
        lot = builder.lot;
        manifest = builder.manifest;
    }

    public static Builder builder() {
        return Builder.builder();
    }

    @Override
    public String eventSource() {
        return SOURCE;
    }

    @Override
    public String eventDetail() {
        return DETAIL;
    }

    public LocatedDataset dataset() {
        return dataset;
    }

    public String lot() {
        return lot;
    }

    public URI manifest() {
        return manifest;
    }

    /**
     * This is a fabricated value for use with pattern matching.
     * <p>
     * We are attempting to bypass any complexity if a listener is listening for two or more datasets.
     *
     * @return a single string for pattern matching
     */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String datasetId() {
        return dataset.id();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ArcNotifyEvent{");
        sb.append("dataset=").append(dataset);
        sb.append(", lot='").append(lot).append('\'');
        sb.append(", manifest=").append(manifest);
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder {
        //TODO: this may be redundant as the dataset can be looked up by role in the arc props
        LocatedDataset dataset;
        String lot;
        URI manifest;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withDataset(LocatedDataset dataset) {
            this.dataset = dataset;
            return this;
        }

        public Builder withLot(String lot) {
            this.lot = lot;
            return this;
        }

        public Builder withManifest(URI manifest) {
            this.manifest = manifest;
            return this;
        }

        public ArcNotifyEvent build() {
            ArcNotifyEvent arcNotifyEvent = new ArcNotifyEvent();
            arcNotifyEvent.lot = this.lot;
            arcNotifyEvent.manifest = this.manifest;
            arcNotifyEvent.dataset = this.dataset;
            return arcNotifyEvent;
        }
    }
}
