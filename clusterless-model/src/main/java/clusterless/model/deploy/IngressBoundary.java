/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

/**
 *
 */
public class IngressBoundary extends Boundary {

    @JsonProperty(required = true)
    private String lotUnit;
    @JsonProperty(required = true)
    private URI listenURI;

    private boolean enableEventBridge = false;

    public String lotUnit() {
        return lotUnit;
    }

    public URI listenURI() {
        return listenURI;
    }

    public boolean enableEventBridge() {
        return enableEventBridge;
    }
}
