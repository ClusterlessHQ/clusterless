/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.model.deploy;

import clusterless.cls.json.JsonRequiredProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 *
 */
@JsonTypeName
public abstract class Activity extends Support {
    @JsonRequiredProperty
    private String name;

    @JsonRequiredProperty
    private String schedule;

    public String name() {
        return name;
    }

    public String schedule() {
        return schedule;
    }
}
