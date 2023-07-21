/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Project;
import clusterless.model.manifest.ManifestState;
import clusterless.naming.Label;
import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.substrate.uri.ManifestURI;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

public class StateURIs {
    public static ManifestURI manifestPath(@NotNull ManagedConstruct managedConstruct, Dataset dataset) {
        Placement placement = placementFor(managedConstruct);
        return manifestPath(placement, dataset);
    }

    @NotNull
    public static ManifestURI manifestPath(Placement placement, Dataset dataset) {
        return ManifestURI.builder()
                .withPlacement(placement)
                .withDataset(dataset)
                .build();
    }

    public static ManifestURI manifestPath(@NotNull ManagedConstruct managedConstruct, ManifestState state, Dataset dataset) {
        return manifestPath(placementFor(managedConstruct), state, dataset);
    }

    @NotNull
    public static ManifestURI manifestPath(Placement placement, ManifestState state, Dataset dataset) {
        return ManifestURI.builder()
                .withPlacement(placement)
                .withDataset(dataset)
                .withState(state)
                .build();
    }

    @NotNull
    public static Project projectFor(@NotNull ManagedConstruct managedConstruct) {
        ManagedProject managedProject = ManagedProject.projectOf(managedConstruct);

        return Project.Builder.builder()
                .withName(managedProject.name().lowerHyphen())
                .withVersion(managedProject.version())
                .build();
    }

    @NotNull
    public static Placement placementFor(@NotNull Construct scope) {
        String account = Stack.of(scope).getAccount();
        String region = Stack.of(scope).getRegion();
        Label stage = StagedApp.stagedOf(scope).stage();

        return Placement.builder()
                .withAccount(account)
                .withRegion(region)
                .withStage(stage.lowerHyphen())
                .build();
    }
}
