/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.aws.lambda;

import clusterless.cls.model.Struct;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Project;
import com.amazonaws.services.lambda.runtime.Context;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.services.glue.model.Column;

import java.io.FileReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public abstract class LambdaHandlerTestBase {
    private Placement placement;
    private Project project;

    protected static Optional<String> loadGradleProperties(String key) {
        Properties properties = new Properties();
        try {
            properties.load(new FileReader("%s/.gradle/gradle.properties".formatted(System.getProperty("user.home"))));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return Optional.ofNullable(properties.getProperty(key));
    }

    protected String defaultRegion() {
        return "us-east-2";
    }

    public Placement defaultPlacement() {
        if (placement == null) {
            placement = Placement.builder()
                    .withStage("prod")
                    .withRegion(defaultRegion())
                    .withAccount("0000000000")
                    .build();
        }
        return placement;
    }

    public Project defaultProject() {
        if (project == null) {
            project = Project.Builder.builder()
                    .withName("test-project")
                    .withVersion("20230101")
                    .build();
        }
        return project;
    }

    protected abstract Struct getProps();

    @NotNull
    protected String eventBusName() {
        return "forwarding-bus";
    }

    @NotNull
    protected String sqsQueueName() {
        return "event-queue";
    }

    @NotNull
    protected String glueDatabaseName() {
        return "gluedatabase";
    }

    protected String glueTableName() {
        return "gluetable";
    }

    List<Column> glueTableColumns() {
        return List.of(
                Column.builder()
                        .name("id")
                        .type("string")
                        .build(),
                Column.builder()
                        .name("name")
                        .type("string")
                        .build()
        );
    }

    List<Column> glueTablePartitions() {
        return List.of(
                Column.builder()
                        .name("lot")
                        .type("string")
                        .build()
        );
    }

    protected Context context() {
        return null;
    }
}
