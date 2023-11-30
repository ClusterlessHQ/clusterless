/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.boundary.s3put;

import clusterless.aws.lambda.boundary.s3put.LotSource;
import clusterless.cls.model.Struct;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Infrequent implements Struct {
    LotSource lotSource = LotSource.objectModifiedTime;

    @JsonIgnore
    String keyRegex;
    boolean enableEventBridge = false;

    public LotSource lotSource() {
        return lotSource;
    }

    public String keyRegex() {
        return keyRegex;
    }

    public boolean enableEventBridge() {
        return enableEventBridge;
    }
}
