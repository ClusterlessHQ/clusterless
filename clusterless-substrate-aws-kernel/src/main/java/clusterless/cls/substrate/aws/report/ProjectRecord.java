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

@JsonPropertyOrder({"placement", "project"})
public class ProjectRecord implements Struct {
    @JsonUnwrapped(prefix = "placement.")
    Placement placement;
    @JsonUnwrapped(prefix = "project.")
    Project project;

    public ProjectRecord(Placement placement, Project project) {
        this.placement = placement;
        this.project = project;
    }

    public Placement placement() {
        return placement;
    }

    public Project project() {
        return project;
    }
}
