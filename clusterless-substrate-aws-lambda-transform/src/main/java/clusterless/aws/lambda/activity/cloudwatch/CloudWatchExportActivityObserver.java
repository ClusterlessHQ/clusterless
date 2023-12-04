/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.activity.cloudwatch;

import clusterless.aws.lambda.EventObserver;

import java.time.Instant;
import java.time.OffsetDateTime;

public interface CloudWatchExportActivityObserver extends EventObserver {

    default boolean enableExport() {
        return true;
    }

    default void applyEvent(OffsetDateTime time) {

    }

    default void applyInterval(String interval) {

    }

    default void applyRange(Instant from, Instant to) {

    }

}
