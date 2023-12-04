/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda;

import clusterless.cls.substrate.aws.sdk.EventBus;
import clusterless.cls.substrate.aws.sdk.Glue;
import clusterless.cls.substrate.aws.sdk.S3;
import clusterless.cls.substrate.aws.sdk.SQS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.services.glue.model.Column;

import java.util.List;

public class BootstrapMachine {
    private static final Logger LOG = LogManager.getLogger(BootstrapMachine.class);
    S3 s3 = new S3();
    EventBus eventBus = new EventBus();
    SQS sqs = new SQS();
    Glue glue = new Glue();

    public BootstrapMachine() {
    }

    public BootstrapMachine logResources() {
        S3.Response list = s3.list();

        list.isSuccessOrThrowRuntime();

        s3.list(list)
                .forEach(bucket -> LOG.info("available bucket: {}", bucket));

        return this;
    }

    public BootstrapMachine applyBucket(boolean enabled, String bucketName) {
        if (!enabled) {
            return this;
        }
        return applyBucket(bucketName);
    }

    public BootstrapMachine applyBucket(String bucketName) {
        if (bucketName == null) {
            return this;
        }

        LOG.info("creating bucket: {}", bucketName);

        s3.create(bucketName)
                .isSuccessOrThrowRuntime();

        LOG.info("created bucket: {}", bucketName);

        return this;
    }

    public BootstrapMachine applyEventbus(String eventBusName) {
        if (eventBusName == null) {
            return this;
        }

        LOG.info("creating eventbus: {}", eventBusName);

        eventBus.create(eventBusName)
                .isSuccessOrThrowRuntime();

        LOG.info("created eventbus: {}", eventBusName);

        return this;
    }

    public BootstrapMachine applySQSQueue(String sqsQueueName) {
        if (sqsQueueName == null) {
            return this;
        }

        LOG.info("creating sqs queue: {}", sqsQueueName);

        sqs.create(sqsQueueName)
                .isSuccessOrThrowRuntime();

        LOG.info("created sqs queue: {}", sqsQueueName);

        return this;
    }

    public BootstrapMachine applyGlueDatabase(boolean usesGlue, String glueDatabase, String glueTable, String location, List<Column> columns, List<Column> columnList) {
        if (glueDatabase == null || !usesGlue) {
            return this;
        }

        LOG.info("creating glue database: {}", glueDatabase);

        glue.createDatabase(glueDatabase)
                .isSuccessOrThrowRuntime();

        glue.createTable(glueDatabase, glueTable, location, columns, columnList)
                .isSuccessOrThrowRuntime();

        LOG.info("created glue database: {}", glueDatabase);

        return this;
    }
}
