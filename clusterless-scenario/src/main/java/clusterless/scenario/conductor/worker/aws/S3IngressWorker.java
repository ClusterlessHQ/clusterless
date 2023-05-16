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
import clusterless.scenario.conductor.task.aws.S3Ingress;
import clusterless.scenario.model.IngressStore;
import clusterless.substrate.aws.sdk.S3;
import clusterless.util.URIs;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class S3IngressWorker implements Worker {
    private static final Logger LOG = LogManager.getLogger(S3IngressWorker.class);

    protected final Options options;

    public S3IngressWorker(Options options) {
        this.options = options;
    }

    @Override
    public String getTaskDefName() {
        return S3Ingress.AWS_S_3_INGRESS;
    }

    @Override
    public TaskResult execute(Task task) {
        Map<String, Object> inputData = task.getInputData();

        LOG.info("ingress worker starting");

        task.setStatus(Task.Status.IN_PROGRESS);

        IngressStore store = JSONUtil.readObjectSafe((String) inputData.get("input"), IngressStore.class);
        Integer delay = (Integer) inputData.get("delay");
        Integer iteration = (Integer) inputData.get("iteration");

        LOG.info("input: {}", delay);
        LOG.info("delay: {}", JSONUtil.writeAsStringSafe(store));
        LOG.info("iteration: {}", iteration);

        // 1 based
        if (iteration != 1) {
            LOG.info("ingress upload sleeping for: {}sec", delay);
            try {
                Thread.sleep(delay * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            LOG.info("ingress upload awake");
        }

        int exitCode = 0;

        URI uri = store.path();

        if (uri == null) {
            throw new IllegalStateException("ingressPath may not be null");
        }

        URI object = URIs.copyAppend(uri, String.format(store.objectName(), iteration, System.currentTimeMillis()));

        String value = IntStream.range(0, store.objectCount())
                .mapToObj(i -> String.format(store.record().value(), i))
                .collect(Collectors.joining("\n"));

        LOG.info("uploading object: {}", object);

        if (options.dryRun()) {
            LOG.info("ingress disabled, dry run");
        } else {
            S3 s3 = new S3(null, store.region());
            S3.Response response = s3.put(object, "application/text", value);

            if (!response.isSuccess()) {
                exitCode = -1;
                LOG.error("unable to write object: {}, error: {}", object, response.errorMessage(), response.exception());
            }
        }

        if (exitCode == 0) {
            return TaskResult.complete();
        }

        return TaskResult.failed("exit code: " + exitCode);
    }
}
