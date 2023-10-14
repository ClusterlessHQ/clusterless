/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.glue.table;

import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.model.deploy.Resource;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates and maintains glue database and any associated metadata.
 */
public class GlueTableResource extends Resource {
    @JsonRequiredProperty
    private String databaseRef;
    @JsonRequiredProperty
    private String tableName;
    private String description;
    private GlueTableSchema schema = new GlueTableSchema();
    @JsonRequiredProperty
    private URI pathURI;
    private boolean removeOnDestroy = false;
    private Map<String, String> tags = new LinkedHashMap<>();

    public GlueTableResource() {
    }

    public String databaseRef() {
        return databaseRef;
    }

    public String tableName() {
        return tableName;
    }

    public String description() {
        return description;
    }

    public GlueTableSchema schema() {
        return schema;
    }

    public URI pathURI() {
        return pathURI;
    }

    public boolean removeOnDestroy() {
        return removeOnDestroy;
    }

    public Map<String, String> tags() {
        return tags;
    }
}
