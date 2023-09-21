/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.uri;

import clusterless.model.deploy.Placement;
import clusterless.model.deploy.Project;
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
}
