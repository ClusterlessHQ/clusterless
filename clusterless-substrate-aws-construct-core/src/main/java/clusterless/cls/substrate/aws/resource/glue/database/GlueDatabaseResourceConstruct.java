/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resource.glue.database;

import clusterless.cls.config.CommonConfig;
import clusterless.cls.substrate.aws.construct.ResourceConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.util.TagsUtil;
import clusterless.commons.naming.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.glue.alpha.Database;

/**
 *
 */
public class GlueDatabaseResourceConstruct extends ResourceConstruct<GlueDatabaseResource> {
    private static final Logger LOG = LogManager.getLogger(GlueDatabaseResourceConstruct.class);

    public GlueDatabaseResourceConstruct(@NotNull ManagedComponentContext context, @NotNull GlueDatabaseResource model) {
        super(context, model, Label.of(model.databaseName()));

        CommonConfig config = context.configurations().get("common");

        boolean removeOnDestroy = config.resource().removeAllOnDestroy() || model().removeOnDestroy();

        if (removeOnDestroy) {
            LOG.info("resource: {}, and all tables will be removed on destroy: {}", model().databaseName(), removeOnDestroy);
        }

        Database database = constructWithinHandler(() -> Database.Builder.create(this, id(model().databaseName()))
                .databaseName(model().databaseName())
                .build());

        database.applyRemovalPolicy(removeOnDestroy ? RemovalPolicy.DESTROY : RemovalPolicy.RETAIN);

        TagsUtil.applyTags(database, model().tags());

        exportArnRefFor(model(), database, database.getDatabaseArn(), "glue database arn");
    }
}
