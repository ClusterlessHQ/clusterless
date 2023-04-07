/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class Arc<W extends Workload<?>> extends Extensible {
    @JsonProperty(required = true)
    String name;
    Map<String, SourceDataset> sources = new LinkedHashMap<>();
    Map<String, SinkDataset> sinks = new LinkedHashMap<>();

    W workload;

    public Arc() {
    }

    public Arc(W workload) {
        this.workload = workload;
    }

    public String name() {
        return name;
    }

    public Map<String, SourceDataset> sources() {
        return sources;
    }

    public Map<String, SinkDataset> sinks() {
        return sinks;
    }

    public W workload() {
        return workload;
    }
}
