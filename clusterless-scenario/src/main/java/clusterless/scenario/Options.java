/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario;

import clusterless.util.Runtimes;
import picocli.CommandLine;

import java.nio.file.Paths;

public class Options {
    @CommandLine.Option(names = "--cls-app", description = "path to the cls-aws kernel")
    private String clsApp = Runtimes.findExecutable("cls").orElse(Paths.get("cls")).toAbsolutePath().toString();

    @CommandLine.Option(names = "--dry-run", description = "do not execute underlying cdk binary")
    private boolean dryRun = false;

    @CommandLine.Option(names = "--verify-on-dry-run", description = "execute verify")
    private boolean verifyOnDryRun = false;

    @CommandLine.Option(names = "--disable-destroy", description = "do not invoke the destroy command, speeds up repeated testing")
    boolean disableDestroy = false;

    public String clsApp() {
        return clsApp;
    }

    public boolean dryRun() {
        return dryRun;
    }

    public boolean verifyOnDryRun() {
        return verifyOnDryRun;
    }

    public boolean disableDestroy() {
        return disableDestroy;
    }
}
