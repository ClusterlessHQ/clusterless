/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.lambda.EventObserver;
import clusterless.model.deploy.SinkDataset;
import clusterless.model.manifest.Manifest;

import java.net.URI;

public interface ArcEventObserver extends EventObserver {
    default void applyFromManifest(Manifest manifest) {
    }

    default void applyToDataset(String role, SinkDataset sinkDataset) {

    }

    default void applyToManifest(String role, URI manifest) {

    }
}
