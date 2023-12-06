/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.process;

import clusterless.cls.config.Configuration;
import clusterless.cls.json.JSONUtil;
import clusterless.cls.startup.Startup;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.core.StopWatch;
import io.github.resilience4j.retry.MaxRetriesExceededException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

public abstract class ProcessExec {
    private static final Logger LOG = LogManager.getLogger(ProcessExec.class);
    protected Supplier<Boolean> dryRun = () -> false;
    protected Supplier<Boolean> retry = () -> false;
    protected Supplier<Integer> verbosity = () -> 0;
    private int retries = 3;

    public ProcessExec() {
    }

    public ProcessExec(Supplier<Boolean> dryRun, Supplier<Integer> verbosity) {
        this.dryRun = dryRun;
        this.verbosity = verbosity;
    }

    public ProcessExec(Supplier<Boolean> dryRun, Supplier<Boolean> retry, Supplier<Integer> verbosity) {
        this.dryRun = dryRun;
        this.retry = retry;
        this.verbosity = verbosity;
    }

    public boolean dryRun() {
        return dryRun.get();
    }

    public boolean retry() {
        return retry.get();
    }

    public int executeProcess(String... args) {
        return executeProcess(Collections.emptyMap(), List.of(args));
    }

    protected int executeProcess(Map<String, String> environment, List<String> args) {
        StopWatch stopWatch = StopWatch.start();
        try {
            if (!retry()) {
                // do not wrap the exec if retry is not enabled
                return process(environment, args);
            }

            LOG.info("enabled retrying command: {} {} times", args, retries);

            RetryConfig config = RetryConfig.<Integer>custom()
                    .maxAttempts(retries)
                    .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofSeconds(30), 2.0))
                    .consumeResultBeforeRetryAttempt((attempt, exitCode) -> LOG.warn("got exit code: {}, for command: {}, retry attempt: {} of {}", exitCode, args, attempt, retries))
                    .retryOnResult(exitCode -> exitCode != 0)
                    .failAfterMaxAttempts(true)
                    .build();

            Retry process = Retry.of("process", config);

            return process.executeCheckedSupplier(() -> process(environment, args));
        } catch (MaxRetriesExceededException e) {
            LOG.error("failed to execute command: {} after {} retries, duration: {}", args, retries, stopWatch.stop(), e);
            return 1;
        } catch (Throwable e) {
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

    protected static List<String> addPropertiesToArgs(Configuration... configs) {
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
