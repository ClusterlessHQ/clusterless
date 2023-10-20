/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.model.manifest;

import clusterless.cls.model.State;
import clusterless.commons.naming.Partition;
import clusterless.commons.util.Strings;

import java.util.Locale;

public enum ManifestState implements State, Partition.EnumPartition {
    complete,
    partial,
    empty,
    removed;

    @Override
    public String key() {
        return "state";
    }

    public boolean hasAttempts() {
        switch (this) {
            case partial:
            case removed:
                return true;
            case empty:
            case complete:
                break;
        }

        return false;
    }

    public static ManifestState parse(String partition) {
        if (Strings.emptyToNull(partition) == null) {
            return null;
        }

        partition = partition.toLowerCase(Locale.ROOT);

        for (ManifestState value : values()) {
            if (partition.matches(String.format("^(.*[=])?%s([.].*)?$", value))) {
                return value;
            }
        }

        return null;
    }
}
