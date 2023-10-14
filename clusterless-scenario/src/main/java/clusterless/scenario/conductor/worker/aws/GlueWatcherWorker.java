/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.conductor.worker.aws;

import clusterless.cls.substrate.aws.sdk.Glue;
import clusterless.scenario.Options;
import clusterless.scenario.conductor.task.aws.GlueWatcher;
import clusterless.scenario.model.WatchedStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.glue.model.Partition;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

public class GlueWatcherWorker extends WatcherWorker {
    private static final Logger LOG = LoggerFactory.getLogger(GlueWatcherWorker.class);

    public GlueWatcherWorker(Options options) {
        super(options);
    }

    @Override
    public String getTaskDefName() {
        return GlueWatcher.AWS_GLUE_WATCHER;
    }

    @Override
    protected int poll(WatchedStore store, URI uri) throws InterruptedException {

        Glue glue = new Glue(null, store.region());

        String catalog = uri.getHost();
        String databaseName = Paths.get(uri.getPath()).getName(0).toString();
        String tableName = Paths.get(uri.getPath()).getName(1).toString();

        while (true) {
            Glue.Response response = glue.listPartitions(catalog, databaseName, tableName, 1000);

            if (!response.isSuccess()) {
                LOG.error("unable to list partitions: {}, error: {}", uri, response.errorMessage());
                return -1;
            }

            List<Partition> objects = glue.listPartitions(response);

            LOG.info("found object in path: {}, count: {}", uri, objects.size());

            if (objects.size() >= store.objectCount()) {
                break;
            }

            Thread.sleep(store.pollingSleepSec() * 1000L);
        }

        return 0;
    }
}
