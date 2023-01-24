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
                "resources" : [
                {
                    "type" : "core:s3Bucket",
                    "bucketName" : "sampleBucket1"
                },
                {
                    "type" : "core:s3Bucket",
                    "bucketName" : "sampleBucket2"
                }
                ]
            }
            """)
    void verify() {
        String[] args = {"--direct", "verify", "-p", "-"};

        new Kernel().execute(args);
    }
}
