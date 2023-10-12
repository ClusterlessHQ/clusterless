/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.json.JsonRequiredProperty;
import clusterless.model.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 *
 */
public class Dataset extends Model {
    @JsonRequiredProperty
    String name;
    @JsonRequiredProperty
    String version;

    public Dataset() {
    }

    public Dataset(Dataset other) {
        this.name = other.name;
        this.version = other.version;
    }

    public Dataset(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public static Builder builder() {
        return Builder.builder();
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    @JsonIgnore
    public String id() {
        return String.format("%s/%s", name(), version());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dataset dataset = (Dataset) o;
        return Objects.equals(name, dataset.name) && Objects.equals(version, dataset.version);
    }

    public boolean sameDataset(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Dataset dataset = (Dataset) o;
        return Objects.equals(name, dataset.name) && Objects.equals(version, dataset.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    public static final class Builder {
        String name;
        String version;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withVersion(String version) {
            this.version = version;
            return this;
        }

        public Dataset build() {
            return new Dataset(name, version);
        }
    }
}
