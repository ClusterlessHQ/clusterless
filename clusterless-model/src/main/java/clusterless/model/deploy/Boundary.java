/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.json.JsonRequiredProperty;

/**
 *
 */
public abstract class Boundary extends Support {
    @JsonRequiredProperty
    String name;

    @JsonRequiredProperty
    Dataset dataset = new Dataset();

    public String name() {
        return name;
    }

    public Dataset dataset() {
        return dataset;
    }
}
