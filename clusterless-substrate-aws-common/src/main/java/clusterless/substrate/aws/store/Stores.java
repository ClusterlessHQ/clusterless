/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.store;

import clusterless.model.deploy.Placement;
import clusterless.naming.Region;
import clusterless.naming.Stage;

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
}
