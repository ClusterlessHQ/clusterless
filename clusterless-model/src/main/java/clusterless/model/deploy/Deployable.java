/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.model.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Deployable extends Model {

    public static final String PROVIDER_POINTER = "/placement/provider";

    @JsonIgnore
    File sourceFile;

    Project project = new Project();
    Placement placement = new Placement();
    @JsonProperty("resources")
    List<Resource> resources = new ArrayList<>();
    @JsonProperty("boundaries")
    List<Boundary> boundaries = new ArrayList<>();
    @JsonProperty("barriers")
    List<Barrier> barriers = new ArrayList<>();
    @JsonProperty("arcs")
    List<Arc> arcs = new ArrayList<>();

    public Deployable() {
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
