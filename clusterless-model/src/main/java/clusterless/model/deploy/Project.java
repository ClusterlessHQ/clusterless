/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.json.JsonRequiredProperty;
import clusterless.managed.component.DocumentsModel;
import clusterless.model.Struct;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.Objects;

/**
 *
 */
@DocumentsModel(
        synopsis = "The project descriptor.",
        description = """
                Names the project to be deployed

                name: The name of the project. Required.
                                
                version: The version of the project. Required.
                         It's recommended to use a date for the version, such as "20230101".
                """
)
@JsonPropertyOrder({"name", "version"})
public class Project implements Struct {
    @JsonRequiredProperty
    String name;
    @JsonRequiredProperty
    String version;

    public Project() {
    }

    public Project(String name, String version) {
        this.name = name;
        this.version = version;
    }

    private Project(Builder builder) {
        name = builder.name;
        version = builder.version;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(name, project.name) && Objects.equals(version, project.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, version);
    }

    /**
     * {@code Project} builder static inner class.
     */
    public static final class Builder {
        private String name;
        private String version;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        /**
         * Sets the {@code name} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code name} to set
         * @return a reference to this Builder
         */
        public Builder withName(String val) {
            name = val;
            return this;
        }

        /**
         * Sets the {@code version} and returns a reference to this Builder enabling method chaining.
         *
         * @param val the {@code version} to set
         * @return a reference to this Builder
         */
        public Builder withVersion(String val) {
            version = val;
            return this;
        }

        /**
         * Returns a {@code Project} built from the parameters previously set.
         *
         * @return a {@code Project} built with parameters of this {@code Project.Builder}
         */
        public Project build() {
            return new Project(this);
        }
    }
}
