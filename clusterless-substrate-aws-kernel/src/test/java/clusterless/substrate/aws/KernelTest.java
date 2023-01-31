/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.StdIo;

/**
 *
 */
public class KernelTest {
    @Test
    @StdIo("""
            {
            "target": {
                "provider": "aws",
                "stage": "prod",
                "account": "abc123",
                "region": "us-east-2"
            },
            "name" : "TestProject",
            "version": "20230101-00",
            "resources" : [
                {
                    "type" : "core:s3Bucket",
                    "bucketName" : "sample-bucket1"
                },
                {
                    "type" : "core:s3Bucket",
                    "bucketName" : "sample-bucket2"
                }
                ]
            }
            """)
    void synth() {
        String[] args = {
                "synth", // runs app.synth() in the current jvm
                "-p",
                "-"
        };

        new Kernel().execute(args);
    }
}
