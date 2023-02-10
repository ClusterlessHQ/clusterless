/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.model.Model;
import clusterless.model.Struct;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Deploy extends Model {

    public static class Project implements Struct {
        String name;
        String version;

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
    }

    public static class Placement implements Struct {
        String provider;
        String stage;
        String account;
        String region;

        public String provider() {
            return provider;
        }

        public String stage() {
            return stage;
        }

        public String account() {
            return account;
        }

        public String region() {
            return region;
        }
    }

    @JsonIgnore
    File sourceFile;

    Project project;
    Placement placement;
    @JsonProperty("resources")
    List<Resource> resources = new ArrayList<>();
    @JsonProperty("boundaries")
    List<Boundary> boundaries = new ArrayList<>();
    @JsonProperty("barriers")
    List<Barrier> barriers = new ArrayList<>();
    @JsonProperty("arcs")
    List<Arc> arcs = new ArrayList<>();

    public Deploy() {
    }

    public File sourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Project project() {
        return project;
    }

    public Placement placement() {
        return placement;
    }

    public List<Resource> resources() {
        return resources;
    }

    public List<Boundary> boundaries() {
        return boundaries;
    }

    public List<Barrier> barriers() {
        return barriers;
    }

    public List<Arc> arcs() {
        return arcs;
    }
}
