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
import clusterless.model.Model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@DocumentsModel(
        synopsis = "A deployable project, the base of a project file",
        description = """
                Use this model to define a deployable project.
                                
                > cls show model --model deployable > project.json
                                
                A project is a collection of resources, boundaries, barriers, and arcs deployed into
                a providers placement environment.
                """
)
public class Deployable extends Model {

    public static final String PROVIDER_POINTER = "/placement/provider";

    @JsonIgnore
    File sourceFile;

    @JsonRequiredProperty
    Project project = new Project();
    @JsonRequiredProperty
    Placement placement = new Placement();
    @JsonProperty("resources")
    List<Resource> resources = new ArrayList<>();
    @JsonProperty("boundaries")
    List<Boundary> boundaries = new ArrayList<>();
    @JsonProperty("barriers")
    List<Barrier> barriers = new ArrayList<>();
    @JsonProperty("arcs")
    List<Arc<? extends Workload>> arcs = new ArrayList<>();

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

    public List<Arc<? extends Workload>> arcs() {
        return arcs;
    }
}
