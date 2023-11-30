/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda.boundary.frequents3put;

import clusterless.aws.lambda.EventObserver;

import java.net.URI;
import java.time.OffsetDateTime;

public interface FrequentPutEventBoundaryObserver extends EventObserver {
    default void applyLotId(String lotId) {

    }

    default void applyDatasetItemsSize(int datasetItemsSize) {

    }

    default void applyManifestURI(URI manifestURI) {

    }

    default void applyEvent(OffsetDateTime time) {

    }

    default void applyIdentifierURI(URI identifierURI) {

    }
}
