/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.cli;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.task.cli.Verifier;

public class VerifierWorker extends ClusterlessWorker {

    public VerifierWorker(Options options) {
        super("verify", options);
    }

    @Override
    public String getTaskDefName() {
        return Verifier.CLS_PROJECT_VERIFIER;
    }

    @Override
    protected boolean getDryRun() {
        return (!super.getDryRun() || !options.verifyOnDryRun()) && super.getDryRun();
    }
}
