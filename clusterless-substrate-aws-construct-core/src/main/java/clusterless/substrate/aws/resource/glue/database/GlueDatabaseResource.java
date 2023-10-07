/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.glue.database;

import clusterless.config.ResourceConfig;
import clusterless.json.JsonRequiredProperty;
import clusterless.model.deploy.Resource;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates and maintains glue database and any associated metadata.
 */
public class GlueDatabaseResource extends Resource {
    @JsonRequiredProperty
    private String databaseName;

    /**
     * When true, the bucket and it's data will be removed when the project is destroyed.
     * <p>
     * Unless the {@link ResourceConfig#removeAllOnDestroy()} is true.
     */
    private boolean removeOnDestroy = false;
    private Map<String, String> tags = new LinkedHashMap<>();

    public GlueDatabaseResource() {
    }

    public String databaseName() {
        return databaseName;
    }

    public boolean removeOnDestroy() {
        return removeOnDestroy;
    }

    public Map<String, String> tags() {
        return tags;
    }
}
