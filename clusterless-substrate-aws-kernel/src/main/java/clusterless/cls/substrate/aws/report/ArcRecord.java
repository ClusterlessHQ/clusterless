/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.model.Struct;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Project;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@JsonPropertyOrder({"placement", "project", "name"})
class ArcRecord implements Struct {
    @JsonUnwrapped(prefix = "placement.")
    Placement placement;
    @JsonUnwrapped(prefix = "project.")
    Project project;
    String name;

    public ArcRecord(Placement placement, Project project, String name) {
        this.placement = placement;
        this.project = project;
        this.name = name;
    }

    public Placement placement() {
        return placement;
    }

    public Project project() {
        return project;
    }

    public String name() {
        return name;
    }
}
