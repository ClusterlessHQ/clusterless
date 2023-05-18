/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.cli;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.task.cli.DeployerBootstrap;

public class DeployerBootstrapWorker extends ClusterlessBootstrapWorker {
    public DeployerBootstrapWorker(Options options) {
        super(false, options);
    }

    @Override
    public String getTaskDefName() {
        return DeployerBootstrap.CLS_PLACEMENT_DEPLOY;
    }
}
