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

public class ReferencedDataset implements Struct {
    Project dependent;
    SourceDataset dataset;

    public ReferencedDataset(Project dependent, SourceDataset dataset) {
        this.dependent = dependent;
        this.dataset = dataset;
    }

    public static Builder builder() {
        return Builder.builder();
    }

    public Project dependent() {
        return dependent;
    }

    public SourceDataset dataset() {
        return dataset;
    }

    @JsonIgnore
    public String id() {
        return String.format("%s:%s", dependent().id(), dataset().id());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReferencedDataset that = (ReferencedDataset) o;
        return Objects.equals(dependent, that.dependent) && Objects.equals(dataset, that.dataset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependent, dataset);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReferencedDataset{");
        sb.append("project=").append(dependent);
        sb.append(", dataset=").append(dataset);
        sb.append('}');
        return sb.toString();
    }

    public static final class Builder {
        Project dependent;
        SourceDataset dataset;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withDependent(Project dependent) {
            this.dependent = dependent;
            return this;
        }

        public Builder withDataset(SourceDataset dataset) {
            this.dataset = dataset;
            return this;
        }

        public ReferencedDataset build() {
            return new ReferencedDataset(dependent, dataset);
        }
    }
}
