/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.cli;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.task.cli.DestroyerBootstrap;

public class DestroyerBootstrapWorker extends ClusterlessBootstrapWorker {
    public DestroyerBootstrapWorker(Options options) {
        super(true, options);
    }

    @Override
    public String getTaskDefName() {
        return DestroyerBootstrap.CLS_PLACEMENT_DESTROY;
    }
}
