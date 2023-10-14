/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.glue.table;

import clusterless.cls.managed.component.ProvidesComponent;
import clusterless.cls.managed.component.ResourceComponentService;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:glueTable",
        synopsis = "Create an AWS Glue Table.",
        description = """
                A simple way to create a managed Glue table within a project.
                                
                tableName: string
                    The name of the table.
                   
                databaseRef: string
                    The ref or arn of the database.
                        
                pathURI: string
                    A URI to the location of the table data.
                            
                schema.columns: {name:..., type:..., comment:...}
                    The columns of the table.
                                
                schema.partitions: {name:..., type:..., comment:...}
                    The partition keys of the table.
                                
                schema.dataFormat: parquet|orc|avro|json|csv|tsv|cloudtrail_logs|apache_logs
                                
                removeOnDestroy: true|false
                    Configured with the following:
                        .removalPolicy(removeOnDestroy ? RemovalPolicy.DESTROY : RemovalPolicy.RETAIN)
                                    
                tags: { key: value, ... }
                    Tags to apply to the bucket.
                """
)
public class GlueTableResourceProvider implements ResourceComponentService<ManagedComponentContext, GlueTableResource, GlueTableResourceConstruct> {

    @Override
    public GlueTableResourceConstruct create(ManagedComponentContext context, GlueTableResource model) {
        return new GlueTableResourceConstruct(context, model);
    }

    @Override
    public Class<GlueTableResource> modelClass() {
        return GlueTableResource.class;
    }
}
