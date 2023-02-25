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

import java.net.URI;

/**
 *
 */
public abstract class Dataset extends Model {
    @JsonProperty(required = true)
    String name;
    @JsonProperty(required = true)
    String version;
    String role;
    @JsonProperty(required = true)
    URI locationURI;

    public String name() {
        return name;
    }

    public String version() {
        return version;
    }

    public String role() {
        return role;
    }

    public URI locationURI() {
        return locationURI;
    }
}
