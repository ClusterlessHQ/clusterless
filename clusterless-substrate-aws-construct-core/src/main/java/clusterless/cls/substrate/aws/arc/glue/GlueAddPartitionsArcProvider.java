/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc.glue;

import clusterless.cls.managed.component.ArcComponentService;
import clusterless.cls.managed.component.ProvidesComponent;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:glueAddPartitionsArc",
        synopsis = "The AWS Glue add partitions arc updates the given catalog with newly arrived partitions.",
        description = """
                This are listens for lot availability events and extracts the partition values from the dataset
                manifest.
                                
                It then adds those partitions to the given AWS Glue table specified by the sink pathURI, having the
                format:
                    glue://[catalog]/database/table
                                
                Where the catalog is only required if writing to a different account.
                                
                The resulting manifest file will contain uris of the format:
                    glue://[catalog]/database/table/value1/value2/...
                                
                partitionType: value|named
                    the default named takes on the form name=value, name is the partition name and value is the
                    partition value
                                
                namedPartitionDelimiter: defaults to '='
                    the delimiter used to separate the partition name from the partition value
                                
                partitionResults: none|all|added
                    none: do not list any partitions in the manifest, just the glue:// uri
                    all: list all partitions found in the source manifest
                    added: list only partitions that did not previously exist in the table
                """
)
public class GlueAddPartitionsArcProvider implements ArcComponentService<ManagedComponentContext, GlueAddPartitionsArc, GlueAddPartitionsArcConstruct> {
    @Override
    public GlueAddPartitionsArcConstruct create(ManagedComponentContext context, GlueAddPartitionsArc model) {
        return new GlueAddPartitionsArcConstruct(context, model);
    }

    @Override
    public Class<GlueAddPartitionsArc> modelClass() {
        return GlueAddPartitionsArc.class;
    }
}
