/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.dataset;

import clusterless.model.deploy.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps ReferencedDataset to a SinkDataset by looking up SourceDatasets in the current set of
 * Deployables or in the metadata store.
 * <p>
 * This prevents a new project from overwriting and existing dataset. And allows current projects
 * to declare read permissions against datasets they do not own.
 */
public class DatasetResolver {
    private static final Logger LOG = LogManager.getLogger(DatasetResolver.class);
    private final List<Deployable> deployables;
    private final Map<Placement, Map<ReferencedDataset, OwnedDataset>> resolved = new HashMap<>();
    private final RemoteDatasetOwnerLookup remoteLookup;

    public DatasetResolver(List<Deployable> deployables) {
        this(deployables, (placement, source) -> Optional.empty());
    }

    public DatasetResolver(List<Deployable> deployables, RemoteDatasetOwnerLookup remoteLookup) {
        this.deployables = deployables;
        this.remoteLookup = remoteLookup;
        build();
    }

    private void build() {
        Multimap<Placement, ReferencedDataset> locallyReferenced = ArrayListMultimap.create();
        Multimap<Placement, OwnedDataset> locallyOwned = ArrayListMultimap.create();

        for (Deployable deployable : deployables) {
            Placement placement = deployable.placement();
            Project project = deployable.project();
            deployable.boundaries().stream()
                    .map(boundary -> new OwnedDataset(project, boundary.dataset()))
                    .forEach(o -> locallyOwned.put(placement, o));

            for (Arc<? extends Workload<?>> arc : deployable.arcs()) {
                arc.sources()
                        .values()
                        .stream()
                        .map(source -> new ReferencedDataset(project, source))
                        .forEach(r -> locallyReferenced.put(placement, r));

                arc.sinks().values()
                        .stream()
                        .map(sink -> new OwnedDataset(project, sink))
                        .forEach(o -> locallyOwned.put(placement, o));
            }
        }

        for (Map.Entry<Placement, Collection<ReferencedDataset>> entry : locallyReferenced.asMap().entrySet()) {
            for (ReferencedDataset referencedDataset : entry.getValue()) {
                Placement placement = entry.getKey();
                Set<OwnedDataset> ownedDatasets = locallyOwned.get(placement).stream()
                        .filter(ownedDataset -> ownedDataset.dataset().sameDataset(referencedDataset.dataset()))
                        .collect(Collectors.toSet());

                // we may want to be strict and always check
                if (ownedDatasets.isEmpty()) {
                    LOG.info("dataset owner not found locally, looking up remotely: {}", referencedDataset.dataset().id());
                    remoteLookup.lookup(placement, referencedDataset.dataset())
                            .ifPresent(ownedDatasets::add);
                }

                if (ownedDatasets.isEmpty()) {
                    String message = "dataset owner not found: %s".formatted(referencedDataset.dataset().id());
                    LOG.error(message);
                    throw new IllegalStateException(message);
                } else if (ownedDatasets.size() != 1) {
                    Set<String> projects = ownedDatasets.stream().map(OwnedDataset::owner).map(Project::id).collect(Collectors.toSet());
                    String message = "dataset: %s, is owned by multiple projects: %s".formatted(referencedDataset.dataset().id(), projects);
                    LOG.error(message);
                    throw new IllegalStateException(message);
                }

                OwnedDataset ownedDataset = ownedDatasets.stream().findFirst().orElseThrow();
                resolved.computeIfAbsent(placement, p -> new HashMap<>())
                        .put(referencedDataset, ownedDataset);
            }
        }
    }

    public Map<String, ? extends LocatedDataset> locate(@NotNull Placement placement, @NotNull Project dependent, @NotNull Map<String, SourceDataset> sources) {
        return sources.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> locate(placement, dependent, e.getValue()).dataset()));
    }

    public OwnedDataset locate(Placement placement, Project dependent, SourceDataset value) {
        ReferencedDataset referencedDataset = new ReferencedDataset(dependent, value);

        if (!resolved.containsKey(placement)) {
            String message = "dataset placement not found for: %s".formatted(placement.id());
            LOG.error(message);
            throw new IllegalStateException(message);
        }

        OwnedDataset ownedDataset = resolved.get(placement).get(referencedDataset);

        if (ownedDataset != null) {
            return ownedDataset;
        }

        String message = "dataset owner not found for: %s".formatted(referencedDataset.dataset().id());
        LOG.error(message);
        throw new IllegalStateException(message);
    }
}
