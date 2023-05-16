/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.store;

import clusterless.naming.Fixed;
import clusterless.naming.Label;

public enum StateStore implements Label {
    Meta("Metadata"),
    Arc("ArcState"),
    Manifest("Manifest");

    final String value;

    StateStore(String value) {
        this.value = value;
    }

    @Override
    public String camelCase() {
        return value;
    }

    public Fixed typeKey() {
        return Fixed.of(lowerCamelCase());
    }

    public Label storeKey() {
        return Label.of(this.with("Store").camelCase());
    }
}
