/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.workload.glue;

import clusterless.aws.lambda.arc.ArcEventHandler;
import clusterless.aws.lambda.arc.ArcEventObserver;
import clusterless.aws.lambda.manifest.AttemptCounter;
import clusterless.aws.lambda.manifest.ManifestReader;
import clusterless.aws.lambda.manifest.ManifestWriter;
import clusterless.cls.model.UriType;
import clusterless.cls.model.deploy.SinkDataset;
import clusterless.cls.model.manifest.Manifest;
import clusterless.cls.model.manifest.ManifestState;
import clusterless.cls.substrate.aws.event.ArcNotifyEvent;
import clusterless.cls.substrate.aws.event.ArcWorkloadContext;
import clusterless.cls.substrate.aws.sdk.Glue;
import clusterless.cls.util.URIs;
import com.amazonaws.services.lambda.runtime.Context;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.glue.model.Table;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 */
public class GlueAddPartitionsArcEventHandler extends ArcEventHandler<GlueAddPartitionsProps> {
    private static final Logger LOG = LogManager.getLogger(GlueAddPartitionsArcEventHandler.class);

    protected static Glue glue = new Glue();
    protected static final AttemptCounter attemptCounter = new AttemptCounter();

    protected ManifestReader manifestReader = new ManifestReader();
    protected Map<String, ManifestWriter> manifestWriters = ManifestWriter.writers(
            arcProps().sinks(),
            arcProps().sinkManifestTemplates(),
            UriType.identifier
    );
    private final Function<String, String> partitionParser = workloadProperties().partitionType() == GlueAddPartitionsProps.PartitionType.named ? this::createNamedPartitionValue : this::createPartitionValue;
    private Table table;

    @Override
    protected Map<String, URI> handleEvent(ArcWorkloadContext arcWorkloadContext, Context context, ArcEventObserver eventObserver) {
        String fromRole = arcWorkloadContext.role();
        ArcNotifyEvent notifyEvent = arcWorkloadContext.arcNotifyEvent();
        String lotId = notifyEvent.lot();
        URI incomingManifestIdentifier = notifyEvent.manifest();

        Manifest incomingManifest = manifestReader.getManifest(incomingManifestIdentifier);

        eventObserver.applyFromManifest(incomingManifestIdentifier, incomingManifest);

        Map<String, URI> result = new LinkedHashMap<>();

        URI fromDatasetPath = notifyEvent.dataset().pathURI();
        Path parent = Paths.get(fromDatasetPath.getPath());
        List<URI> fromUris = incomingManifest.uris();

        Set<Path> uniquePaths = fromUris.stream()
                .map(u -> parent.relativize(Paths.get(u.getPath())))
                .map(p -> incomingManifest.uriType() == UriType.path ? p : p.getParent()) // remove filename or prefix if not a path
                .collect(Collectors.toSet());

        Map<URI, List<String>> partitions = uniquePaths.stream()
                .collect(Collectors.toMap(
                        p -> URIs.copyAppendAsPath(fromDatasetPath, p.toString()),
                        p -> {
                            List<String> partitionValues = new LinkedList<>();
                            for (int i = 0; i < p.getNameCount(); i++) {
                                partitionValues.add(partitionParser.apply(p.getName(i).getFileName().toString()));
                            }
                            return partitionValues;
                        }
                ));

        for (Map.Entry<String, SinkDataset> sinkRoleEntry : arcProps().sinks().entrySet()) {
            String toRole = sinkRoleEntry.getKey();
            ManifestWriter manifestWriter = manifestWriters.get(toRole);

            if (incomingManifest.state() == ManifestState.empty) {
                LOG.info("manifest state empty, role: {} -> {}", fromRole, toRole);
                URI manifestURI = manifestWriter.writeEmptyManifest(lotId);
                result.put(toRole, manifestURI);

                eventObserver.applyToManifest(toRole, manifestURI);
                continue;
            }

            SinkDataset sinkDataset = sinkRoleEntry.getValue();

            eventObserver.applyToDataset(toRole, sinkDataset);

            URI toDatasetPath = sinkDataset.pathURI(); // glue://catalog/database/table

            List<URI> results = uniquePaths.stream().map(p -> URIs.copyAppend(toDatasetPath, p.toString()))
                    .toList();

            Path path = Paths.get(toDatasetPath.getPath());

            if (path.getNameCount() < 2) {
                throw new RuntimeException("invalid glue path: %s, expects the form: glue://[catalog]/database/table, where catalog is optional".formatted(toDatasetPath));
            }

            String catalog = toDatasetPath.getHost(); // null ok
            String databaseName = path.getName(0).getFileName().toString();
            String tableName = path.getName(1).getFileName().toString();

            // get descriptor from table and reuse it with the new partition and location information
            Table table = tableDescriptor(databaseName, tableName);

            LOG.info("database: {}, table: {}, storageDescriptor: {}", table.databaseName(), table.name(), table.storageDescriptor());

            List<String> errors = new LinkedList<>();
            List<List<String>> failed = new LinkedList<>();
            List<List<String>> exists = new LinkedList<>();

            Stream<Map<URI, List<String>>> batches = batch(partitions, 100);

            batches.forEach(batch -> {
                Glue.Response response = glue.addPartitions(catalog, table, batch);

                // fail if the api call outright fails
                response.isSuccessOrThrowRuntime(
                        r -> String.format("unable to create partitions in: %s/%s, %s", databaseName, tableName, r.errorMessage())
                );

                // if the batch request has errors, log them
                if (glue.hasBatchErrors(response)) {
                    glue.batchErrors(response, partitionError -> {
                        // need to disambiguate duplicates vs other errors
                        LOG.info("batch error: {}", partitionError);
                        String errorCode = partitionError.errorDetail().errorCode();

                        // unsure if this is the right way to disambiguate
                        if ("AlreadyExistsException".equals(errorCode)) {
                            exists.add(partitionError.partitionValues());
                        } else {
                            errors.add(partitionError.toString());
                            failed.add(partitionError.partitionValues());
                        }
                    });
                }
            });

            List<URI> success;
            switch (workloadProperties().partitionResults()) {
                case none -> {
                    success = List.of(toDatasetPath);
                }
                case all -> {
                    success = results;
                }
                case added -> {
                    Set<URI> remove = exists.stream()
                            .map(p -> URIs.copyAppend(toDatasetPath, p.toString()))
                            .collect(Collectors.toSet());
                    success = results.stream()
                            .filter(r -> !remove.contains(r))
                            .toList();
                }
                default -> {
                    throw new IllegalStateException("unexpected value: " + workloadProperties().partitionResults());
                }
            }

            // they all failed, throw an exception
            if (failed.size() == partitions.size()) {
                String errorMessages = errors.stream().limit(5).collect(Collectors.joining(", "));
                LOG.error("failed to add partitions, role: {} -> {}, having: {}, partitions: {}, errors: {}", fromRole, toRole, fromUris.size(), partitions.size(), errorMessages);
                throw new RuntimeException("of partitions: %s, succeeded: %s, failed: %s, with errors: %s".formatted(partitions.size(), partitions.size() - exists.size(), failed.size(), errorMessages));
            }

            URI manifestURI;

            // some failed
            if (!failed.isEmpty()) {
                String errorMessages = errors.stream().limit(5).collect(Collectors.joining(", "));
                LOG.warn("failed to add partitions, role: {} -> {}, having: {}, partitions: {}, errors: {}", fromRole, toRole, fromUris.size(), partitions.size(), errorMessages);
                manifestURI = manifestWriter.writePartialManifest(
                        success,
                        lotId,
                        attemptCounter.attemptId(context.getAwsRequestId()),
                        "of partitions: %s, succeeded: %s, failed: %s, with errors: %s".formatted(partitions.size(), partitions.size() - exists.size(), failed.size(), errorMessages)
                );
            } else if (exists.size() == partitions.size()) {
                LOG.info("no partitions added with no errors, role: {} -> {}, having: {}", fromRole, toRole, fromUris.size());
                manifestURI = manifestWriter.writeEmptyManifest(lotId);
            } else {
                LOG.info("successfully added partitions with no errors, role: {} -> {}, having: {}, partitions: {}", fromRole, toRole, fromUris.size(), partitions.size());
                manifestURI = manifestWriter.writeSuccessManifest(success, lotId);
            }

            result.put(toRole, manifestURI);

            eventObserver.applyToManifest(toRole, manifestURI);
        }

        return result;
    }

    private Stream<Map<URI, List<String>>> batch(Map<URI, List<String>> partitions, int partitionSize) {
        if (partitions.size() <= partitionSize) {
            return Stream.of(partitions);
        }

        return Streams.stream(Iterables.partition(partitions.entrySet(), partitionSize))
                .map(e -> e.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    private Table tableDescriptor(String databaseName, String tableName) {
        if (table != null) {
            return table;
        }

        Glue.Response tableResponse = glue.getTable(databaseName, tableName);

        tableResponse.isSuccessOrThrowRuntime(
                r -> String.format("unable to get table descriptor for: %s/%s, %s", databaseName, tableName, r.errorMessage())
        );

        table = Glue.getTable(tableResponse);

        return table;
    }

    @NotNull
    private String createPartitionValue(String path) {
        return path;
    }

    private String createNamedPartitionValue(String path) {
        int i = path.indexOf(workloadProperties().namedPartitionDelimiter());
        return path.substring(i + 1);
    }
}
