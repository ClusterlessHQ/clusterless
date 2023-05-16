/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.config.Configuration;
import clusterless.json.JSONUtil;
import clusterless.startup.Startup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public abstract class ProcessExec {
    private static final Logger LOG = LogManager.getLogger(ProcessExec.class);
    protected Supplier<Boolean> dryRun = () -> false;

    public ProcessExec() {
    }

    public ProcessExec(Supplier<Boolean> dryRun) {
        this.dryRun = dryRun;
    }

    public boolean dryRun() {
        return dryRun.get();
    }

    protected int executeProcess(String... args) {
        return executeProcess(Collections.emptyMap(), List.of(args));
    }

    protected int executeProcess(Map<String, String> environment, List<String> args) {
        try {
            return process(environment, args);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int process(Map<String, String> parentEnv, List<String> args) throws IOException, InterruptedException {
        Map<String, String> environment = getCommonEnvironment();

        environment.putAll(parentEnv);

        if (!environment.isEmpty()) {
            LOG.info("environment: {}", environment);
        }

        LOG.info("command: {}", args);

        if (dryRun()) {
            LOG.warn("dry run, not executing command: {}", args);
            return 0;
        }

        ProcessBuilder processBuilder = new ProcessBuilder(args)
                .inheritIO();

        processBuilder.environment().putAll(environment);

        Process process = processBuilder.start();

        int exitCode = process.waitFor();

        LOG.info("completed with exit code: {}", exitCode);

        return exitCode;
    }

    @NotNull
    protected static List<String> addPropertiesToArgs(@NotNull Configuration... configs) {
        List<String> apArgs = new LinkedList<>();
        Map<String, String> properties = new LinkedHashMap<>();

        for (Configuration config : configs) {
            properties.putAll(JSONUtil.asMapSafe(config.name(), config));
        }

        Startup.asPropertyArgs(apArgs, properties);

        return apArgs;
    }

    protected abstract Map<String, String> getCommonEnvironment();
}
