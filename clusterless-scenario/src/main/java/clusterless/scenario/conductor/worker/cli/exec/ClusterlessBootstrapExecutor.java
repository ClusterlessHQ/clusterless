/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.cli.exec;

import clusterless.cls.util.SafeList;

import java.util.List;
import java.util.Map;

public class ClusterlessBootstrapExecutor extends BaseClusterlessExecutor {
    private final Map<String, String> placement;
    private final boolean destroy;

    protected ClusterlessBootstrapExecutor(String clsApp, boolean dryRun, String workingDirectory, Map<String, String> placement, boolean destroy) {
        super(clsApp, dryRun, workingDirectory);
        this.placement = placement;
        this.destroy = destroy;
    }

    @Override
    protected void createCommand(List<String> args) {
        //cls bootstrap --account ... --region ... --stage ...
        args.addAll(SafeList.of(
                "bootstrap",
                "--account",
                placement.get("account"),
                "--region",
                placement.get("region"),
                "--stage",
                placement.get("stage"),
                "-v",
                destroy ? "--destroy" : null,
                destroy ? "--retry" : null
        ));
    }

    public static final class Builder {
        private String clsApp;
        private boolean dryRun;
        private String workingDirectory;
        private Map<String, String> placement;
        private boolean destroy;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withClsApp(String clsApp) {
            this.clsApp = clsApp;
            return this;
        }

        public Builder withDryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public Builder withWorkingDirectory(String workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public Builder withPlacement(Map<String, String> placement) {
            this.placement = placement;
            return this;
        }

        public Builder withDestroy(boolean destroy) {
            this.destroy = destroy;
            return this;
        }

        public ClusterlessBootstrapExecutor build() {
            return new ClusterlessBootstrapExecutor(clsApp, dryRun, workingDirectory, placement, destroy);
        }
    }
}
