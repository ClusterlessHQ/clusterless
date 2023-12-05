/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.construct;

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
public class ExtensibleConstruct<E extends Extensible> extends ManagedConstruct {
    private static final Logger LOG = LogManager.getLogger(ExtensibleConstruct.class);

    private final Map<String, IBucket> buckets = new HashMap<>(); // cache the construct to prevent collisions
    private final Map<String, ITable> tables = new HashMap<>(); // cache the construct to prevent collisions
    private final E model;

    public ExtensibleConstruct(@NotNull ManagedComponentContext context, @NotNull E model) {
        super(context, uniqueId(model, Label.of(model.name())));
        this.model = model;
    }

    public ExtensibleConstruct(@NotNull ManagedComponentContext context, @NotNull E model, @NotNull Label discriminator) {
        super(context, uniqueId(model, discriminator));
        this.model = model;
    }

    private static Label uniqueId(@NotNull Extensible model, @NotNull Label discriminator) {
        return model
                .label()
                .with(discriminator);
    }

    @NotNull
    public E model() {
        return model;
    }

    public String name() {
        return model().name();
    }

    protected String id(String value) {
        return model()
                .label()
                .with(Label.of(value))
                .camelCase();
    }

    protected <R extends IConstruct> R constructWithinHandler(Supplier<R> supplier) {
        if (model() != null) {
            return ErrorsUtil.construct(model().type(), supplier, LOG);
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
