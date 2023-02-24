/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.model.Model;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Arc extends Model {
    @JsonProperty(required = true)
    String name;
    List<SourceDataset> sources = new ArrayList<>();
    @JsonProperty(required = true)
    Workload workload;
    List<SinkDataset> sinks = new ArrayList<>();

    public List<SourceDataset> sources() {
        return sources;
    }

    public Workload workload() {
        return workload;
    }

    public List<SinkDataset> sinks() {
        return sinks;
    }
}
