/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.glue.table;

import clusterless.model.deploy.Resource;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates and maintains glue database and any associated metadata.
 */
public class GlueTableResource extends Resource {
    private String databaseRef;
    private String tableName;
    private GlueTableSchema schema = new GlueTableSchema();
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
