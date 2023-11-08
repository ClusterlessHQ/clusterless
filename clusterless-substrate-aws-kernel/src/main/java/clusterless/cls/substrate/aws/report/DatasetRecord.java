/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.model.Struct;
import clusterless.cls.model.deploy.Dataset;
import clusterless.cls.model.deploy.Placement;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

@JsonPropertyOrder({"placement", "dataset"})
public class DatasetRecord implements Struct {
    @JsonUnwrapped(prefix = "placement.")
    Placement placement;
    @JsonUnwrapped(prefix = "dataset.")
    Dataset dataset;

    public DatasetRecord(Placement placement, Dataset dataset) {
        this.placement = placement;
        this.dataset = dataset;
    }

    public Placement placement() {
        return placement;
    }

    public Dataset dataset() {
        return dataset;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatasetRecord{");
        sb.append("placement=").append(placement);
        sb.append(", dataset=").append(dataset);
        sb.append('}');
        return sb.toString();
    }
}
