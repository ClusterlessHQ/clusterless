/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.construct;

import clusterless.cls.model.Model;
import clusterless.cls.model.deploy.Extensible;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.managed.ManagedConstruct;
import clusterless.cls.substrate.aws.util.ErrorsUtil;
import clusterless.commons.naming.Label;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.glue.alpha.ITable;
import software.amazon.awscdk.services.glue.alpha.Table;
import software.amazon.awscdk.services.glue.alpha.TableAttributes;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;
import software.constructs.IConstruct;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 *
 */
public class ModelConstruct<M extends Model> extends ManagedConstruct {
    private static final Logger LOG = LogManager.getLogger(ModelConstruct.class);

    private final Map<String, IBucket> buckets = new HashMap<>(); // cache the construct to prevent collisions
    private final Map<String, ITable> tables = new HashMap<>(); // cache the construct to prevent collisions
    private final M model;

    public ModelConstruct(@NotNull ManagedComponentContext context, @NotNull M model, @NotNull String id) {
        super(context, uniqueId(model, id));
        this.model = model;
    }

    private static Label uniqueId(@NotNull Model model, @NotNull String id) {
        return model
                .label()
                .with(id);
    }

    public M model() {
        return model;
    }

    protected String id(String value) {
        return model()
                .label()
                .with(Label.of(value))
                .camelCase();
    }

    protected <R extends IConstruct> R constructWithinHandler(Supplier<R> supplier) {
        if (model() instanceof Extensible) {
            return ErrorsUtil.construct(((Extensible) model()).type(), supplier, LOG);
        }

        return ErrorsUtil.construct(null, supplier, LOG);
    }

    @NotNull
    protected IBucket getBucketFor(String baseId, String bucketName) {
        return buckets.computeIfAbsent(bucketName, k -> Bucket.fromBucketName(this, baseId, k));
    }

    @NotNull
    protected ITable getTableFor(String baseId, String tableName) {
        return tables.computeIfAbsent(tableName, k -> Table.fromTableAttributes(this, baseId, TableAttributes.builder()
                .tableName(k)
                .build())
        );
    }
}
