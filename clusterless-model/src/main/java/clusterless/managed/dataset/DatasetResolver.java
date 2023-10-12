/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.dataset;

import clusterless.model.deploy.*;
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
    List<Deployable> deployables;

    Map<ReferencedDataset, OwnedDataset> resolved = new HashMap<>();
    private List<ReferencedDataset> locallyReferenced;
    private List<OwnedDataset> locallyOwned;

    public DatasetResolver(List<Deployable> deployables) {
        this.deployables = deployables;

        build();
    }

    private void build() {
        locallyReferenced = new LinkedList<>();
        locallyOwned = new LinkedList<>();

        for (Deployable deployable : deployables) {
            deployable.boundaries().stream()
                    .map(boundary -> new OwnedDataset(deployable.project(), boundary.dataset()))
                    .forEach(locallyOwned::add);

            for (Arc<? extends Workload<?>> arc : deployable.arcs()) {
                arc.sources()
                        .values()
                        .stream()
                        .map(source -> new ReferencedDataset(deployable.project(), source))
                        .forEach(locallyReferenced::add);

                arc.sinks().values()
                        .stream()
                        .map(sink -> new OwnedDataset(deployable.project(), sink))
                        .forEach(locallyOwned::add);
            }
        }

        for (ReferencedDataset referencedDataset : locallyReferenced) {
            Set<OwnedDataset> ownedDatasets = locallyOwned.stream()
                    .filter(ownedDataset -> ownedDataset.dataset().sameDataset(referencedDataset.dataset()))
                    .collect(Collectors.toSet());

            // todo: confirm dataset isn't already owned outside this set of deployables

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

            resolved.put(referencedDataset, ownedDatasets.stream().findFirst().orElseThrow());
        }
    }

    public List<OwnedDataset> locallyOwned() {
        return locallyOwned;
    }

    public Map<String, ? extends LocatedDataset> locate(@NotNull Project dependent, @NotNull Map<String, SourceDataset> sources) {
        return sources.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> locate(dependent, e.getValue()).dataset()));
    }

    public OwnedDataset locate(Project dependent, SourceDataset value) {
        ReferencedDataset referencedDataset = new ReferencedDataset(dependent, value);
        OwnedDataset ownedDataset = resolved.get(referencedDataset);

        if (ownedDataset != null) {
            return ownedDataset;
        }

        String message = "dataset owner not found for: %s".formatted(referencedDataset.dataset().id());
        LOG.error(message);
        throw new IllegalStateException(message);
    }
}
