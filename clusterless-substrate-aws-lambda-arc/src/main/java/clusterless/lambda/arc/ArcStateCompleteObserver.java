/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.lambda.EventObserver;

import java.net.URI;
import java.util.Map;

public interface ArcStateCompleteObserver extends EventObserver {
    void applySinkManifests(Map<String, URI> sinkStates);

    void applyWorkloadError(Map<String, Object> workloadError);
}
