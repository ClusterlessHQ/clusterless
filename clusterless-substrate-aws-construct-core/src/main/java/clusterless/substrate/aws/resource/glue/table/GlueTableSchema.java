/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.glue.table;

import clusterless.json.JsonRequiredProperty;
import clusterless.model.Struct;

import java.util.LinkedList;
import java.util.List;

public class GlueTableSchema implements Struct {
    public static class GlueColumn implements Struct {
        String name;
        String type;
        String comment;

        public String name() {
            return name;
        }

        public String type() {
            return type;
        }

        public String comment() {
            return comment;
        }
    }

    @JsonRequiredProperty
    List<GlueColumn> columns = new LinkedList<>();
    List<GlueColumn> partitions = new LinkedList<>();

    @JsonRequiredProperty
    String dataFormat;

    public List<GlueColumn> columns() {
        return columns;
    }

    public List<GlueColumn> partitions() {
        return partitions;
    }

    public String dataFormat() {
        return dataFormat;
    }
}
