/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.report;

import clusterless.cls.model.Struct;
import clusterless.cls.model.state.ArcState;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;


@JsonPropertyOrder({"arcRecord", "lotId", "arcState"})
public class ArcStatusRecord implements Struct {
    @JsonUnwrapped
    @JsonProperty("arc")
    ArcRecord arcRecord;
    String lotId;
    ArcState arcState;

    public ArcStatusRecord(ArcRecord arcRecord, String lotId, ArcState arcState) {
        this.arcRecord = arcRecord;
        this.lotId = lotId;
        this.arcState = arcState;
    }

    public ArcRecord arcRecord() {
        return arcRecord;
    }

    public String lotId() {
        return lotId;
    }

    public ArcState arcState() {
        return arcState;
    }
}
