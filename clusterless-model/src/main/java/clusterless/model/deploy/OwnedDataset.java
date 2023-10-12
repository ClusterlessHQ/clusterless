/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.model.Struct;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class OwnedDataset implements Struct {
    Project owner;
    SinkDataset dataset;

    public OwnedDataset(Project owner, SinkDataset dataset) {
        this.owner = owner;
        this.dataset = dataset;
    }

    public static Builder builder() {
        return Builder.builder();
    }

    public Project owner() {
        return owner;
    }

    public SinkDataset dataset() {
        return dataset;
    }

    @JsonIgnore
    public String id() {
        return String.format("%s:%s", owner().id(), dataset().id());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OwnedDataset that = (OwnedDataset) o;
        return Objects.equals(owner, that.owner) && Objects.equals(dataset, that.dataset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, dataset);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OwnedDataset{");
        sb.append("owner=").append(owner);
        sb.append(", dataset=").append(dataset);
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder {
        Project owner;
        SinkDataset dataset;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withOwner(Project owner) {
            this.owner = owner;
            return this;
        }

        public Builder withDataset(SinkDataset dataset) {
            this.dataset = dataset;
            return this;
        }

        public OwnedDataset build() {
            return new OwnedDataset(owner, dataset);
        }
    }
}
