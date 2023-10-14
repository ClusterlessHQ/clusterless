/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.cli.exec;

import clusterless.cls.startup.Startup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class BaseClusterlessExecutor {
    private static final Logger LOG = LogManager.getLogger(ClusterlessProjectExecutor.class);
    private static final Map<String, String> properties = new LinkedHashMap<>();

    static {
        properties.put("common.resource.removeAllOnDestroy", "true");
        properties.put("aws.cdk.requireDeployApproval", "never");
        properties.put("aws.cdk.requireDestroyApproval", "false");
    }

    protected final String clsApp;
    protected final boolean dryRun;
    protected final String workingDirectory;

    public BaseClusterlessExecutor(String clsApp, boolean dryRun, String workingDirectory) {
        this.clsApp = clsApp;
        this.dryRun = dryRun;
        this.workingDirectory = workingDirectory;
    }

    private Map<String, String> getEnvironment() {
        return Collections.emptyMap();
    }

    public int exec() {
        List<String> args = new LinkedList<>();

        args.add(clsApp);

        Startup.asPropertyArgs(args, properties);

        createCommand(args);

        if (dryRun) {
            args.add("--dry-run");
        }

        LOG.info("working directory: {}", workingDirectory);

        Map<String, String> environment = getEnvironment();

        if (!environment.isEmpty()) {
            LOG.info("environment: {}", environment);
        }

        LOG.info("command: {}", args);

        try {
            Path cwd = Paths.get(workingDirectory);

            Files.createDirectories(cwd);

            Path outPath = cwd.resolve("out.log");
            Path errorPath = cwd.resolve("err.log");

            ProcessBuilder processBuilder = new ProcessBuilder()
                    .command(args)
                    .directory(cwd.toFile())
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(outPath.toFile()))
                    .redirectError(ProcessBuilder.Redirect.appendTo(errorPath.toFile()));

            processBuilder.environment().putAll(environment);

            Process process = processBuilder.start();

            return process.waitFor();
        } catch (IOException e) {
            LOG.error("unable to start command: " + args, e);
            return -1;
        } catch (InterruptedException e) {
            LOG.error("unable to wait for command: " + args, e);
            return -1;
        }
    }

    protected abstract void createCommand(List<String> args);
}
