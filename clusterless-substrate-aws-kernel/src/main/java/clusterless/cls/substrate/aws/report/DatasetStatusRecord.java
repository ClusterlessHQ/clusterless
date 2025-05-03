/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.model.Struct;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.substrate.uri.ManifestURI;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;


@JsonPropertyOrder({"dataset", "lotId", "manifestState"})
public class DatasetStatusRecord implements StatusRecord<ManifestState>, Struct {
    @JsonUnwrapped
    @JsonProperty("dataset")
    DatasetRecord datasetRecord;
    String lotId;
    ManifestState manifestState;
    @JsonUnwrapped
    ManifestURI uri;

    public DatasetStatusRecord(DatasetRecord record, ManifestURI uri) {
        this.datasetRecord = record;
        this.lotId = uri.lotId();
        this.manifestState = uri.state();
        this.uri = uri.withPlacement(record.placement());
    }

    public DatasetRecord datasetRecord() {
        return datasetRecord;
    }

    public String lotId() {
        return lotId;
    }

    public ManifestState manifestState() {
        return manifestState;
    }

    @Override
    public ManifestState state() {
        return manifestState();
    }

    public ManifestURI uri() {
        return uri;
    }
}
