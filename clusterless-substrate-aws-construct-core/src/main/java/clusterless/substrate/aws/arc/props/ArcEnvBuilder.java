/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.arc.props;

import clusterless.lambda.arc.ArcProps;
import clusterless.managed.dataset.DatasetOwnerLookup;
import clusterless.model.deploy.*;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.substrate.aws.event.ArcWorkloadContext;
import clusterless.substrate.aws.resources.StateURIs;
import clusterless.substrate.uri.ManifestURI;
import clusterless.util.Env;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Known env vars
 * <pre>
 *     AWS_BATCH_JOB_ATTEMPT
 *     AWS_BATCH_JQ_NAME
 *     MANAGED_BY_AWS
 *     ECS_AGENT_URI
 *     ECS_CONTAINER_METADATA_URI
 *     AWS_DEFAULT_REGION
 *     AWS_EXECUTION_ENV
 *     AWS_REGION
 *     ECS_CONTAINER_METADATA_URI_V4
 *     AWS_BATCH_CE_NAME
 *     AWS_BATCH_JOB_ID
 * </pre>
 */
public class ArcEnvBuilder {
    private final Placement placement;
    private final ArcProps<WorkloadProps> arcProps;
    private final Map<String, SourceDataset> sources;
    private final Map<String, SinkDataset> sinks;
    private final WorkloadProps workloadProps;

    public ArcEnvBuilder(Placement placement, Arc<? extends Workload<? extends WorkloadProps>> model) {
        this.placement = placement;
        this.sources = model.sources();
        this.sinks = model.sinks();
        this.workloadProps = model.workload().workloadProps();
        this.arcProps = createArcProps();
    }

    protected ArcProps<WorkloadProps> createArcProps() {
        Map<String, ManifestURI> sourceManifestPaths = sources
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> StateURIs.manifestPath(placement, ManifestState.complete, e.getValue())));

        Map<String, ManifestURI> sinkManifestPaths = sinks
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> StateURIs.manifestPath(placement, e.getValue())));

        return ArcProps.builder()
                .withSources(sources)
                .withSinks(sinks)
                .withSourceManifestPaths(sourceManifestPaths)
                .withSinkManifestTemplates(sinkManifestPaths)
                .withWorkloadProps(workloadProps)
                .build();
    }

    public ArcProps<WorkloadProps> arcProps() {
        return arcProps;
    }

    public Map<String, String> asEnvironment() {
        return Env.toEnv(arcProps());
    }

    @NotNull
    public ArcWorkloadContext execContext(String role, String lotId, ManifestState manifestState, DatasetOwnerLookup lookupOwner) {
        URI manifest = arcProps.sourceManifestPaths()
                .get(role)
                .withState(manifestState)
                .withLot(lotId)
                .uri();

        SourceDataset source = sources.get(role);

        OwnedDataset sink = lookupOwner.lookup(source); // throws an error if not found

        return ArcWorkloadContext.builder()
                .withArcNotifyEvent(
                        ArcNotifyEvent.Builder.builder()
                                .withDataset(sink.dataset())
                                .withManifest(manifest)
                                .withLot(lotId)
                                .build()
                )
                .withRole(role)
                .build();
    }
}
