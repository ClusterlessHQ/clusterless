/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resource.glue.table;

import clusterless.config.CommonConfig;
import clusterless.substrate.aws.construct.ResourceConstruct;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.util.TagsUtil;
import clusterless.util.URIs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.glue.alpha.*;
import software.amazon.awscdk.services.s3.IBucket;

import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class GlueTableResourceConstruct extends ResourceConstruct<GlueTableResource> {
    private static final Logger LOG = LogManager.getLogger(GlueTableResourceConstruct.class);

    public GlueTableResourceConstruct(@NotNull ManagedComponentContext context, @NotNull GlueTableResource model) {
        super(context, model, model.tableName());

        CommonConfig config = context.configurations().get("common");

        boolean removeOnDestroy = config.resource().removeAllOnDestroy() || model().removeOnDestroy();

        if (removeOnDestroy) {
            LOG.info("resource: {}, and all tables will be removed on destroy: {}", model().tableName(), removeOnDestroy);
        }

        LOG.info("using database resolving ref: {}", model().databaseRef());

        IDatabase database = resolveArnRef(model().databaseRef(), arn -> {
            LOG.info("using database arn: {}", arn);
            return Database.fromDatabaseArn(this, "Database", arn);
        });

        URI uri = model().pathURI();

        IBucket tableLocation = getBucketFor("TableLocation", uri.getHost());

        Table table = constructWithinHandler(() -> Table.Builder.create(this, id(model().tableName()))
                .database(database)
                .tableName(model().tableName())
                .description(model().description())
                .bucket(tableLocation)
                .s3Prefix(URIs.asKeyPath(model.pathURI()))
                .columns(columnsFrom(model.schema().columns()))
                .partitionKeys(columnsFrom(model.schema().partitions()))
                // partition indexes should be an option for highly dimensioned tables
                // https://docs.aws.amazon.com/glue/latest/dg/partition-indexes.html#partition-index-1
                // https://docs.aws.amazon.com/athena/latest/ug/glue-best-practices.html#glue-best-practices-partition-index
                .enablePartitionFiltering(false)
                .dataFormat(formatFrom(model.schema().dataFormat()))
                // .encryption(TableEncryption.S3_MANAGED) // cannot be set if setting a bucket
                .build());

        table.applyRemovalPolicy(removeOnDestroy ? RemovalPolicy.DESTROY : RemovalPolicy.RETAIN);

        TagsUtil.applyTags(table, model().tags());

        addArnRefFor(model(), table, table.getTableArn(), "glue table arn");
    }

    private DataFormat formatFrom(String format) {
        Objects.requireNonNull(format, "format is required");
        switch (format.toLowerCase()) {

            case "parquet" -> {
                return DataFormat.PARQUET;
            }

            case "csv" -> {
                return DataFormat.CSV;
            }

            case "tsv" -> {
                return DataFormat.TSV;
            }

            case "json" -> {
                return DataFormat.JSON;
            }

            case "orc" -> {
                return DataFormat.ORC;
            }

            case "cloudtrail_logs" -> {
                return DataFormat.CLOUDTRAIL_LOGS;
            }

            case "apache_logs" -> {
                return DataFormat.APACHE_LOGS;
            }

            default -> throw new IllegalStateException("Unexpected value: " + format);
        }
    }

    private List<? extends Column> columnsFrom(List<GlueTableSchema.GlueColumn> columns) {
        return columns
                .stream()
                .map(column -> Column.builder()
                        .name(column.name())
                        .type(Type.builder()
                                .inputString(column.type())
                                .isPrimitive(true)
                                .build())
                        .comment(column.comment())
                        .build()).toList();
    }
}
