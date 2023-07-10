/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.cli.exec;

import java.util.Collections;
import java.util.List;

public class ClusterlessProjectExecutor extends BaseClusterlessExecutor {
    protected final String command;
    protected final String projectFiles;
    protected final List<String> extraArgs;

    protected ClusterlessProjectExecutor(String clsApp, boolean dryRun, String command, String workingDirectory, String projectFiles) {
        this(clsApp, dryRun, command, workingDirectory, projectFiles, Collections.emptyList());
    }

    protected ClusterlessProjectExecutor(String clsApp, boolean dryRun, String command, String workingDirectory, String projectFiles, List<String> extraArgs) {
        super(clsApp, dryRun, workingDirectory);
        this.command = command;
        this.projectFiles = projectFiles;
        this.extraArgs = extraArgs;
    }

    @Override
    protected void createCommand(List<String> args) {
        //cls deploy -p test-s3-copy-arc-project-chain.json
        args.addAll(List.of(
                command,
                "-p",
                projectFiles,
                "-v"
        ));

        args.addAll(extraArgs);
    }

    public static final class Builder {
        private String clsApp;
        private boolean dryRun;
        private String command;
        private String workingDirectory;
        private String projectFiles;
        protected List<String> extraArgs = Collections.emptyList();

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

        public Builder withCommand(String command) {
            this.command = command;
            return this;
        }

        public Builder withWorkingDirectory(String workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public Builder withProjectFiles(String projectFiles) {
            this.projectFiles = projectFiles;
            return this;
        }

        public Builder withExtraArgs(List<String> extraArgs) {
            this.extraArgs = extraArgs;
            return this;
        }

        public ClusterlessProjectExecutor build() {
            return new ClusterlessProjectExecutor(clsApp, dryRun, command, workingDirectory, projectFiles, extraArgs);
        }
    }
}
