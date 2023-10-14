/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.meta;

import clusterless.cls.json.JSONUtil;
import clusterless.cls.model.deploy.*;
import clusterless.cls.substrate.aws.cdk.CDKProcessExec;
import clusterless.cls.substrate.aws.cdk.bootstrap.BootstrapMeta;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.uri.ArcURI;
import clusterless.cls.substrate.uri.DatasetURI;
import clusterless.cls.substrate.uri.ProjectMaterialsURI;
import clusterless.cls.substrate.uri.ProjectURI;
import com.fasterxml.jackson.core.type.TypeReference;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Metadata {
    private static final Logger LOG = LoggerFactory.getLogger(Metadata.class);

    public static void writeBootstrapMetaLocal(BootstrapMeta bootstrapMeta) {
        writeMetaLocal(bootstrapMeta, Metadata::createBootstrapMetaPath);
    }

    @NotNull
    public static Path createBootstrapMetaPath(Path outputPath) {
        return outputPath.resolve("bootstrap").resolve("meta.json");
    }

    public static void writeProjectMetaLocal(List<Deployable> deployables) {
        writeMetaLocal(deployables, Metadata::createProjectMetaPath);
    }

    @NotNull
    public static Path createProjectMetaPath(Path outputPath) {
        return outputPath.resolve("deployables").resolve("project.json");
    }

    private static void writeMetaLocal(Object struct, Function<Path, Path> pathResolver) {
        Path outputPath = CDKProcessExec.cdkLocalOutputPath();

        if (outputPath == null) {
            return;
        }

        Path path = pathResolver.apply(outputPath);
        LOG.info("writing metadata to: {}", path.toAbsolutePath());

        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        JSONUtil.writeAsStringSafe(path, struct);
    }

    public static int pushBootstrapMetadata(String profile, String region, String outputPath, boolean dryRun) {
        Path bootstrapMetaPath = createBootstrapMetaPath(Paths.get(outputPath));

        LOG.info("reading metadata from: {}", bootstrapMetaPath.toAbsolutePath());

        if (dryRun) {
            LOG.info("dry run, skipping metadata upload");
            return 0;
        }

        BootstrapMeta bootstrapMeta = JSONUtil.readAsObjectSafe(bootstrapMetaPath, BootstrapMeta.class);

        S3 s3 = new S3(profile, region);

        URI metaURI = S3.createS3URI(bootstrapMeta.exports().get("metadata").name(), "metadata.json");

        LOG.info("putting metadata in: {}", metaURI);

        Optional<Throwable> result = s3.put(metaURI, "application/json", bootstrapMeta)
                .isSuccessOrLog(r -> String.format("unable to upload bootstrap metadata to: %s, %s", metaURI, r.errorMessage()));

        return result.isPresent() ? 1 : 0;
    }

    public static int pushDeployablesMetadata(String outputPath, boolean dryRun) {
        return deployablesMetadata(outputPath, dryRun, Metadata::pushDeployablesMetadata);
    }

    public static int removeDeployablesMetadata(String outputPath, boolean dryRun) {
        return deployablesMetadata(outputPath, dryRun, Metadata::removeDeployablesMetadata);
    }

    protected static int deployablesMetadata(String outputPath, boolean dryRun, Function<List<Deployable>, Integer> f) {
        Path projectMetaPath = createProjectMetaPath(Paths.get(outputPath));

        LOG.info("reading metadata from: {}", projectMetaPath.toAbsolutePath());

        if (dryRun) {
            LOG.info("dry run, skipping metadata upload");
            return 0;
        }

        List<Deployable> deployables;
        try {
            deployables = JSONUtil.readAsObject(projectMetaPath, new TypeReference<>() {});
        } catch (IOException e) {
            LOG.info("unable to read metadata from: {}", projectMetaPath.toAbsolutePath(), e);
            return 1;
        }

        return f.apply(deployables);
    }

    public static int pushDeployablesMetadata(List<Deployable> deployables) {
        String profile = System.getenv().get(CDKProcessExec.CLS_CDK_PROFILE);

        for (Deployable deployable : deployables) {
            List<URI> materials = new ArrayList<>();

            Placement placement = deployable.placement();
            Project project = deployable.project();

            S3 s3 = new S3(profile, placement.region());

            URI metaURI = ProjectURI.builder()
                    .withPlacement(placement)
                    .withProject(project)
                    .build()
                    .uri();

            materials.add(metaURI);
            LOG.info("putting metadata in: {}", metaURI);

            Optional<Throwable> result = s3.put(metaURI, "application/json", deployable)
                    .isSuccessOrLog(r -> String.format("unable to upload project metadata to: %s, %s", metaURI, r.errorMessage()));

            if (result.isPresent()) {
                return 1;
            }

            List<SinkDataset> sinks = new LinkedList<>();

            for (Arc<? extends Workload<?>> arc : deployable.arcs()) {
                URI arcURI = ArcURI.builder()
                        .withPlacement(placement)
                        .withProject(project)
                        .withArcName(arc.name())
                        .build()
                        .uri();

                materials.add(arcURI);
                LOG.info("putting metadata in: {}", arcURI);

                result = s3.put(arcURI, "application/json", arc)
                        .isSuccessOrLog(r -> String.format("unable to upload arc metadata to: %s, %s", arcURI, r.errorMessage()));

                if (result.isPresent()) {
                    return 1;
                }

                sinks.addAll(arc.sinks().values());
            }

            for (Boundary boundary : deployable.boundaries()) {
                // todo: upload boundary metadata
                sinks.add(boundary.dataset());
            }

            for (SinkDataset sinkDataset : sinks) {
                URI datasetURI = DatasetURI.builder()
                        .withPlacement(placement)
                        .withDataset(sinkDataset)
                        .build()
                        .uri();

                materials.add(datasetURI);
                LOG.info("putting metadata in: {}", datasetURI);

                result = s3.put(datasetURI, "application/json", new OwnedDataset(project, sinkDataset))
                        .isSuccessOrLog(r -> String.format("unable to upload dataset metadata to: %s, %s", datasetURI, r.errorMessage()));

                if (result.isPresent()) {
                    return 1;
                }
            }

            URI materialsURI = ProjectMaterialsURI.builder()
                    .withPlacement(placement)
                    .withProject(project)
                    .build()
                    .uri();

            LOG.info("putting metadata in: {}", materialsURI);

            Optional<Throwable> materialsResults = s3.put(materialsURI, "application/json", materials)
                    .isSuccessOrLog(r -> String.format("unable to upload materials metadata to: %s, %s", metaURI, r.errorMessage()));

            if (materialsResults.isPresent()) {
                return 1;
            }
        }

        return 0;
    }

    public static int removeDeployablesMetadata(List<Deployable> deployables) {
        String profile = System.getenv().get(CDKProcessExec.CLS_CDK_PROFILE);

        for (Deployable deployable : deployables) {
            Placement placement = deployable.placement();
            Project project = deployable.project();

            S3 s3 = new S3(profile, placement.region());

            URI uri = ProjectMaterialsURI.builder()
                    .withPlacement(placement)
                    .withProject(project)
                    .build()
                    .uri();


            LOG.info("removing metadata in: {}", uri);

            S3.Response response = s3.get(uri);

            if (!s3.exists(response)) {
                throw new IllegalStateException("materials not found: " + uri, response.exception());
            }

            List<URI> materials = JSONUtil.readAsObjectSafe(response.asInputStream(), new TypeReference<>() {});

            boolean failed = false;
            for (URI material : materials) {
                LOG.info("removing metadata in: {}", material);

                Optional<Throwable> result = s3.remove(material)
                        .isSuccessOrLog(r -> String.format("unable to remove material metadata to: %s, %s", material, r.errorMessage()));

                failed |= result.isPresent();
            }

            Optional<Throwable> result = s3.remove(uri)
                    .isSuccessOrLog(r -> String.format("unable to remove materials metadata to: %s, %s", uri, r.errorMessage()));

            if (result.isPresent() || failed) {
                return 1;
            }
        }

        return 0;
    }

}
