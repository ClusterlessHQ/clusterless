/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.state;

import clusterless.model.State;
import clusterless.util.Partition;
import clusterless.util.Strings;

import java.util.Locale;

/**
 * <pre>
 *  running-->complete;
 *  running-->partial;
 *  running-->empty;
 *  partial-->running;
 *  partial-->missing;
 *  missing-->running;
 * </pre>
 */
public enum ArcState implements State, Partition.EnumPartition {
    running,
    complete,
    partial,
    empty,
    missing;

    @Override
    public String key() {
        return "state";
    }

    public static ArcState parse(String state) {
        if (Strings.emptyToNull(state) == null) {
            return null;
        }

        state = state.toLowerCase(Locale.ROOT);

        for (ArcState value : values()) {
            if (state.matches(String.format("^(.*[=])?%s([.].*)?$", value))) {
                return value;
            }
        }

        return null;
    }

}
