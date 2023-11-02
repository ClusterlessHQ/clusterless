/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.uri;

import clusterless.cls.model.deploy.Dataset;
import clusterless.cls.model.deploy.Placement;
import clusterless.cls.model.deploy.Project;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MetaURITest {
    @Test
    void project() {
        ProjectURI projectURI = ProjectURI.builder()
                .withPlacement(Placement.builder()
                        .withAccount("00000000")
                        .withRegion("us-west-2")
                        .withProvider("aws")
                        .withStage("prod")
                        .build())
                .withProject(Project.Builder.builder()
                        .withName("test-project")
                        .withVersion("20230101")
                        .build())
                .build();

        assertFalse(projectURI.isPath());
        assertEquals(URI.create("s3://prod-clusterless-metadata-00000000-us-west-2/projects/name=test-project/version=20230101/project.json"), projectURI.uri());
        assertEquals(projectURI.uri(), ProjectURI.parse(projectURI.template()).uri());
    }

    @Test
    void projectKey() {
        Project project = Project.Builder.builder()
                .withName("test-project")
                .withVersion("20230101")
                .build();

        assertEquals(project, ProjectURI.parse("/projects/name=test-project/version=20230101/project.json").project());
    }

    @Test
    void dataset() {
        DatasetURI datasetURI = DatasetURI.builder()
                .withPlacement(Placement.builder()
                        .withAccount("00000000")
                        .withRegion("us-west-2")
                        .withProvider("aws")
                        .withStage("prod")
                        .build())
                .withDataset(Dataset.Builder.builder()
                        .withName("test-project")
                        .withVersion("20230101")
                        .build())
                .build();

        assertFalse(datasetURI.isPath());
        assertEquals(URI.create("s3://prod-clusterless-metadata-00000000-us-west-2/datasets/name=test-project/version=20230101/dataset.json"), datasetURI.uri());
        assertEquals(datasetURI.uri(), DatasetURI.parse(datasetURI.template()).uri());
    }

    @Test
    void datasetKey() {
        Project project = Project.Builder.builder()
                .withName("test-project")
                .withVersion("20230101")
                .build();

        assertEquals(project, ProjectURI.parse("/datasets/name=test-project/version=20230101/dataset.json").project());
    }

    @Test
    void arcKey() {
        Project project = Project.Builder.builder()
                .withName("test-project")
                .withVersion("20230101")
                .build();

        assertEquals(project, ArcURI.parse("/datasets/name=test-project/version=20230101/arc=testArc/arc.json").project());
        assertEquals("testArc", ArcURI.parse("/datasets/name=test-project/version=20230101/arc=testArc/arc.json").arcName());
    }
}
