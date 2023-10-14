/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.aws;

import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.scenario.Options;
import clusterless.scenario.conductor.task.aws.S3Watcher;
import clusterless.scenario.model.WatchedStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

public class S3WatcherWorker extends WatcherWorker {
    private static final Logger LOG = LoggerFactory.getLogger(S3WatcherWorker.class);

    public S3WatcherWorker(Options options) {
        super(options);
    }

    @Override
    public String getTaskDefName() {
        return S3Watcher.AWS_S_3_WATCHER;
    }

    @Override
    protected int poll(WatchedStore store, URI uri) throws InterruptedException {
        S3 s3 = new S3(null, store.region());

        while (true) {
            S3.Response response = s3.listObjects(uri);

            if (!response.isSuccess()) {
                LOG.error("unable to list path: {}, error: {}", uri, response.errorMessage());
                return -1;
            }

            List<String> objects = s3.listChildren(response);

            LOG.info("found object in path: {}, count: {}", uri, objects.size());

            if (objects.size() >= store.objectCount()) {
                break;
            }

            Thread.sleep(store.pollingSleepSec() * 1000L);
        }

        return 0;
    }
}
