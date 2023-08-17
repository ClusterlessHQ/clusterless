/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.aws;

import clusterless.json.JSONUtil;
import clusterless.scenario.Options;
import clusterless.scenario.model.WatchedStore;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.*;

public abstract class WatcherWorker implements Worker {
    private static final Logger LOG = LogManager.getLogger(S3WatcherWorker.class);
    protected final Options options;

    public WatcherWorker(Options options) {
        this.options = options;
    }

    @Override
    public abstract String getTaskDefName();

    @Override
    public TaskResult execute(Task task) {
        Map<String, Object> inputData = task.getInputData();

        LOG.info("watcher worker starting");

        task.setStatus(Task.Status.IN_PROGRESS);

        WatchedStore store = JSONUtil.readObjectSafe((String) inputData.get("input"), WatchedStore.class);

        LOG.info("input: {}", JSONUtil.writeAsStringSafe(store));

        int exitCode = 0;

        URI uri = store.path();

        if (uri == null) {
            throw new IllegalStateException("ingressPath may not be null");
        }

        LOG.info("watching path: {}", uri);

        if (options.dryRun()) {
            LOG.info("watcher disabled, dry run");
        } else {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Integer> result = executorService.submit(() -> poll(store, uri));

            try {
                exitCode = result.get(store.timeoutSec(), TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException e) {
                LOG.error("watcher timed out with: {}", e.getMessage());
                exitCode = -1;
            } catch (ExecutionException e) {
                LOG.error("watcher failed with: {}", e.getMessage(), e);
                exitCode = -1;
            }
        }

        if (exitCode == 0) {
            task.setOutputData(Map.of("complete", store.path().toString()));

            return TaskResult.complete();
        }

        return TaskResult.failed("exit code: " + exitCode);
    }

    protected abstract int poll(WatchedStore store, URI uri) throws InterruptedException;
}
