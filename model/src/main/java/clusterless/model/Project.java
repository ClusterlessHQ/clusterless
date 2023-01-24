/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Project extends Model {
    @JsonProperty("resources")
    List<Resource> resources = new ArrayList<>();
    List<Boundary> boundaries = new ArrayList<>();
    List<Barrier> barriers = new ArrayList<>();
    List<Arc> arcs = new ArrayList<>();

    public Project() {
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
