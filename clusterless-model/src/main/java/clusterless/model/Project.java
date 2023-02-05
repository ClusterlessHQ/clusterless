/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model;

import clusterless.util.Label;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Project extends Model {
    public static class Target implements Struct {
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

    Target target;
    String name;
    String version;
    @JsonProperty("resources")
    List<Resource> resources = new ArrayList<>();
    @JsonProperty("boundaries")
    List<Boundary> boundaries = new ArrayList<>();
    @JsonProperty("barriers")
    List<Barrier> barriers = new ArrayList<>();
    @JsonProperty("arcs")
    List<Arc> arcs = new ArrayList<>();

    public Project() {
    }

    public File sourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public Target target() {
        return target;
    }

    public String name() {
        return name;
    }

    public String version() {
        return version;
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

    @Override
    public Label label() {
        return Label.of("Project");
    }
}
