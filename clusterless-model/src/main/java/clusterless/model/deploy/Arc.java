/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.managed.component.DocumentsModel;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
@DocumentsModel(
        synopsis = "The arc descriptor.",
        description = """
                Declares the arc to be deployed, the workload, and any workload properties.

                type: The component of the Arc to be deployed. Required.
                      Such as, "aws:core:s3CopyArc" for the S3CopyArc component. 
                                
                name: The name of the arc. Required.
                                
                sources: The source datasets of the arc. Required.
                         The dataset name is the key, the SourceDataset descriptor is the value.
                                
                sinks: The sink datasets of the arc. Required.
                       The dataset name is the key, the SinkDataset descriptor is the value.
                                
                workload: The workload to be deployed. Optional.
                          See the documentation for the arc component for details.
                """
)
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
