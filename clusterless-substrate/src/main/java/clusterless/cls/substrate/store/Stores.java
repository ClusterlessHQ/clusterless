/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.store;

import clusterless.cls.model.deploy.Placement;
import clusterless.cls.naming.Region;
import clusterless.cls.naming.Stage;

import java.util.List;
import java.util.Objects;

public class Stores {

    public static String bootstrapStoreName(StateStore stateStore, Placement placement) {
        return bootstrapStoreName(stateStore, placement.account(), Region.of(placement.region()), Stage.of(placement.stage()));
    }

    public static String bootstrapStoreName(StateStore stateStore, String account, Region region, Stage stage) {
        Objects.requireNonNull(stateStore, "stateStore is null");
        Objects.requireNonNull(account, "account is null");
        Objects.requireNonNull(region, "region is null");

        return stage.asLower()
                .with("Clusterless")
                .with(stateStore)
                .with(account)
                .with(region)
                .lowerHyphen();
    }

    public static List<Placement> parseBootstrapStoreNames(StateStore stateStore, List<String> bootstrapStoreNames) {
        String filter = "-clusterless-%s".formatted(stateStore.name().toLowerCase());

        return bootstrapStoreNames.stream()
                .filter(n -> n.contains(filter))
                .map(Stores::parseBootstrapStoreName)
                .toList();
    }

    public static Placement parseBootstrapStoreName(String bootstrapStoreName) {
        Objects.requireNonNull(bootstrapStoreName, "bootstrapStoreName is null");

        String[] parts = bootstrapStoreName.split("-", 5);

        return Placement.builder()
                .withStage(parts[0])
                .withAccount(parts[3])
                .withRegion(parts[4])
                .build();
    }

}
