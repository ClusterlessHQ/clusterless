/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.sdk;

import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.glue.GlueClient;
import software.amazon.awssdk.services.glue.model.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * SELECT * FROM "<table_name>$partitions" to get count
 */
public class Glue extends ClientBase<GlueClient> {
    public Glue() {
    }

    public Glue(String profile) {
        super(profile);
    }

    public Glue(String profile, String region) {
        super(profile, region);
    }

    @NotNull
    protected String getEndpointEnvVar() {
        return "AWS_GLUE_ENDPOINT";
    }

    @Override
    protected GlueClient createClient(String region) {
        logEndpointOverride();

        return GlueClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .endpointOverride(endpointOverride)
                .build();
    }

    public Response createDatabase(String databaseName) {
        CreateDatabaseRequest request = CreateDatabaseRequest.builder()
                .databaseInput(DatabaseInput.builder()
                        .name(databaseName)
                        .build())
                .build();

        try (GlueClient glueClient = createClient()) {
            return new Response(glueClient.createDatabase(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response createTable(String databaseName, String tableName, String location, List<Column> columns, List<Column> partitionKeys) {
        CreateTableRequest request = CreateTableRequest.builder()
                .databaseName(databaseName)
                .tableInput(TableInput.builder()
                        .name(tableName)
                        .storageDescriptor(StorageDescriptor.builder()
                                .columns(columns)
                                .location(location)
                                .build())
                        .partitionKeys(partitionKeys)
                        .build())
                .build();

        try (GlueClient glueClient = createClient()) {
            return new Response(glueClient.createTable(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public Response getTable(String databaseName, String tableName) {
        GetTableRequest request = GetTableRequest.builder()
                .databaseName(databaseName)
                .name(tableName)
                .build();

        try (GlueClient glueClient = createClient()) {
            return new Response(glueClient.getTable(request));
        } catch (Exception exception) {
            return new Response(exception);
        }
    }

    public static Table getTable(Glue.Response tableResponse) {
        Objects.requireNonNull(tableResponse.awsResponse);

        return ((GetTableResponse) tableResponse.awsResponse).table();
    }

    public Response listPartitions(String catalog, String databaseName, String tableName, int maxResults) {
        GetPartitionsRequest request = GetPartitionsRequest.builder()
                .catalogId(catalog)
                .databaseName(databaseName)
                .tableName(tableName)
                .maxResults(maxResults)
                .build();

        try (GlueClient glueClient = createClient()) {
            return new Response(glueClient.getPartitions(request));
        } catch (Exception exception) {
            // possible exceptions:
            // software.amazon.awssdk.services.glue.model.AccessDeniedException
            return new Response(exception);
        }
    }

    public List<Partition> listPartitions(Response response) {
        Objects.requireNonNull(response.awsResponse);

        return ((GetPartitionsResponse) response.awsResponse).partitions();
    }

    public Response addPartitions(String catalog, Table table, Map<URI, List<String>> partitions) {
        Objects.requireNonNull(table, "table");

        String databaseName = table.databaseName();
        String tableName = table.name();
        StorageDescriptor storageDescriptor = table.storageDescriptor();

        return addPartitions(catalog, storageDescriptor, databaseName, tableName, partitions);
    }

    public Response addPartitions(String catalog, StorageDescriptor storageDescriptor, String databaseName, String tableName, Map<URI, List<String>> partitions) {
        if (partitions.size() > 100) {
            throw new IllegalArgumentException("cannot add more than 100 partitions at a time, got: " + partitions.size());
        }

        Instant now = Instant.now();

        List<PartitionInput> partitionInputList = partitions.entrySet().stream()
                .map(
                        partition -> PartitionInput.builder()
                                .values(partition.getValue())
                                .storageDescriptor(storageDescriptor
                                        .toBuilder()
                                        .location(partition.getKey().toString())
                                        .build()
                                )
                                .lastAccessTime(now)
                                .lastAnalyzedTime(now)
                                .build()
                ).toList();

        BatchCreatePartitionRequest request = BatchCreatePartitionRequest.builder()
                .catalogId(catalog)
                .databaseName(databaseName)
                .tableName(tableName)
                .partitionInputList(partitionInputList)
                .build();

        try (GlueClient glueClient = createClient()) {
            return new Response(glueClient.batchCreatePartition(request));
        } catch (Exception exception) {
            // possible exceptions:
            // software.amazon.awssdk.services.glue.model.AccessDeniedException
            return new Response(exception);
        }
    }

    public boolean hasBatchErrors(Response response) {
        if (response.awsResponse == null) {
            throw new IllegalArgumentException("response.awsResponse cannot be null");
        }

        return ((BatchCreatePartitionResponse) response.awsResponse).hasErrors() && !((BatchCreatePartitionResponse) response.awsResponse).errors().isEmpty();
    }

    public void batchErrors(Response response, Consumer<PartitionError> errors) {
        if (response.awsResponse == null) {
            throw new IllegalArgumentException("response.awsResponse cannot be null");
        }

        // https://docs.aws.amazon.com/glue/latest/webapi/API_BatchCreatePartition.html#API_BatchCreatePartition_RequestSyntax
        ((BatchCreatePartitionResponse) response.awsResponse).errors()
                .forEach(errors);
    }
}
