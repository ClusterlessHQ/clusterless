/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.local;

import clusterless.command.LocalCommandOptions;
import clusterless.process.ProcessExec;

import java.util.Map;

public class LocalProcessExec extends ProcessExec {
    public LocalProcessExec(LocalCommandOptions commandOptions) {
        super(commandOptions::dryRun);
    }

    @Override
    protected Map<String, String> getCommonEnvironment() {
        return null;
    }
}
