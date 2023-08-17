/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda;

import clusterless.substrate.store.StateStore;
import clusterless.substrate.store.Stores;
import clusterless.util.Env;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Optional;

@Testcontainers
@ExtendWith(SystemStubsExtension.class)
public abstract class LocalStackBase extends LambdaHandlerTestBase {
    static {
        System.setProperty("clusterless.localstack.enabled", "true");
    }

    static DockerImageName localstackImage = DockerImageName.parse("localstack/localstack-pro:2.2.0");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(localstackImage)
            .withEnv("LOCALSTACK_API_KEY", Optional.ofNullable(System.getenv("LOCALSTACK_API_KEY"))
                    .or(() -> loadGradleProperties("localstack.api.key")).orElseThrow())
            .withServices(
                    LocalStackContainer.Service.S3,
                    LocalStackContainer.Service.SQS,
                    LocalStackContainer.EnabledService.named("events"),
                    LocalStackContainer.EnabledService.named("glue")
            );

    @Override
    protected String defaultRegion() {
        return localstack.getRegion();
    }

    @SystemStub
    private EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(Env.keyTyped(getProps()), Env.valueTyped(getProps())) // shove the props json into an env var
            .set(Env.keyJSON(getProps()), Env.value(getProps())) // shove the props json into an env var
            .set("AWS_ACCESS_KEY_ID", localstack.getAccessKey())
            .set("AWS_SECRET_ACCESS_KEY", localstack.getSecretKey())
            .set("AWS_DEFAULT_REGION", localstack.getRegion())
            .set("AWS_S3_ENDPOINT", localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString())
            .set("AWS_EVENTS_ENDPOINT", localstack.getEndpointOverride(LocalStackContainer.EnabledService.named("events")).toString())
            .set("AWS_GLUE_ENDPOINT", localstack.getEndpointOverride(LocalStackContainer.EnabledService.named("glue")).toString())
            .set("AWS_SQS_ENDPOINT", localstack.getEndpointOverride(LocalStackContainer.Service.SQS).toString());

    @BeforeEach
    public void bootstrap() {
        new BootstrapMachine()
                .applyBucket(Stores.bootstrapStoreName(StateStore.Manifest, defaultPlacement()))
                .applyBucket(Stores.bootstrapStoreName(StateStore.Arc, defaultPlacement()))
                .applyBucket(Stores.bootstrapStoreName(StateStore.Meta, defaultPlacement()))
                .applyEventbus(eventBusName())
                .applySQSQueue(sqsQueueName())
                .applyBucket(glueDatabaseName())
                .applyGlueDatabase(glueDatabaseName(), glueTableName(), "s3://%s".formatted(glueDatabaseName()), glueTableColumns(), glueTablePartitions());
    }
}
