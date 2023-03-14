/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.StdIo;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static clusterless.substrate.aws.resources.Assets.CLS_ASSETS_PATH;

/**
 *
 */
@ExtendWith(SystemStubsExtension.class)
public class KernelTest {

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(CLS_ASSETS_PATH, "build"); // we only need to point to a dir, we don't need the asset

    private String[] args = new String[]{
            "synth", // runs app.synth() in the current jvm
            "-p",
            "-"
    };

    @Test
    @StdIo("""
            {
            "project": {
                "name" : "TestProject",
                "version": "20230101-00"
            },
            "placement": {
                "stage": "prod",
                "provider": "aws",
                "account": "abc123",
                "region": "us-east-2"
            },
            "resources" : [
                {
                    "type" : "aws:core:s3Bucket",
                    "bucketName" : "sample-bucket1"
                },
                {
                    "type" : "aws:core:s3Bucket",
                    "bucketName" : "sample-bucket2"
                }
                ]
            }
            """)
    void createResourcesProject() {
        Assertions.assertEquals(0, new Kernel().execute(args));
    }

    @Test
    @StdIo("""
            {
              "project": {
                "name": "TestProject",
                "version": "20230101-00"
              },
              "placement": {
                "stage": "prod",
                "provider": "aws",
                "account": "abc123",
                "region": "us-east-2"
              },
              "arcs": [
                {
                  "type": "aws:core:s3CopyArc",
                  "name": "copyWorkload",
                  "sources": {
                    "main": {
                      "name": "ingress",
                      "version": "20220101",
                      "pathURI": "s3://clusterless-test/ingress/"
                    }
                  },
                  "sinks": {
                    "main": {
                      "name": "copy",
                      "version": "20230101",
                      "pathURI": "s3://clusterless-test/copy/"
                    }
                  }
                }
              ]
            }
                     \s""")
    void copyArcProject() {
        Assertions.assertEquals(0, new Kernel().execute(args));
    }
}
