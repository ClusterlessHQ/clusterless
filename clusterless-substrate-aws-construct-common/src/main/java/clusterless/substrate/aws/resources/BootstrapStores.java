/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.substrate.aws.managed.StagedApp;
import clusterless.substrate.aws.store.StateStore;
import clusterless.substrate.aws.store.Stores;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Fn;
import software.constructs.Construct;

import static clusterless.substrate.aws.store.StateStore.*;

/**
 *
 */
public class BootstrapStores {

    public static String metadataStoreName(@NotNull Construct scope) {
        return bootstrapStoreName(scope, Meta);
    }

    public static String arcStateStoreName(@NotNull Construct scope) {
        return bootstrapStoreName(scope, Arc);
    }

    public static String arcStateStoreNameRef(@NotNull Construct scope) {
        return bootstrapStoreNameRef(scope, Arc);
    }

    public static String manifestStoreName(@NotNull Construct scope) {
        return bootstrapStoreName(scope, Manifest);
    }

    public static String manifestStoreNameRef(@NotNull Construct scope) {
        return bootstrapStoreNameRef(scope, Manifest);
    }

    private static String bootstrapStoreName(@NotNull Construct scope, StateStore bucketName) {
        return Stores.bootstrapStoreName(bucketName, StateURIs.placementFor(scope));
    }

    private static String bootstrapStoreNameRef(@NotNull Construct scope, StateStore name) {
        Label stage = StagedApp.stagedOf(scope).stage();
        return Fn.importValue(stage.with(name.storeNameKey()).lowerHyphen());
    }
}