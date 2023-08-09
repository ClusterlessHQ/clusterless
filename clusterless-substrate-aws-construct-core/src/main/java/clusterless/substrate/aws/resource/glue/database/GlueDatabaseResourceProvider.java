/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.glue.database;

import clusterless.managed.component.ProvidesComponent;
import clusterless.managed.component.ResourceComponentService;
import clusterless.substrate.aws.managed.ManagedComponentContext;

/**
 *
 */
@ProvidesComponent(
        type = "aws:core:glueDatabase",
        synopsis = "Create an AWS Glue Database.",
        description = """
                A simple way to create a managed Glue database within a project.
                                
                name: string
                    The name referenced as databaseRef in aws:core:glueTable.
                               
                databaseName: string
                    The name of the database. Regionally unique.
                    
                removeOnDestroy: true|false
                    Configured with the following:
                        .removalPolicy(removeOnDestroy ? RemovalPolicy.DESTROY : RemovalPolicy.RETAIN)
                                    
                tags: { key: value, ... }
                    Tags to apply to the bucket.
                """
)
public class GlueDatabaseResourceProvider implements ResourceComponentService<ManagedComponentContext, GlueDatabaseResource, GlueDatabaseResourceConstruct> {

    @Override
    public GlueDatabaseResourceConstruct create(ManagedComponentContext context, GlueDatabaseResource model) {
        return new GlueDatabaseResourceConstruct(context, model);
    }

    @Override
    public Class<GlueDatabaseResource> modelClass() {
        return GlueDatabaseResource.class;
    }
}
