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
    @StdIo("{\n" +
           "\"project\": {\n" +
           "    \"name\" : \"TestProject\",\n" +
           "    \"version\": \"20230101-00\"\n" +
           "},\n" +
           "\"placement\": {\n" +
           "    \"stage\": \"prod\",\n" +
           "    \"provider\": \"aws\",\n" +
           "    \"account\": \"abc123\",\n" +
           "    \"region\": \"us-east-2\"\n" +
           "},\n" +
           "\"resources\" : [\n" +
           "    {\n" +
           "        \"type\" : \"aws:core:s3Bucket\",\n" +
           "        \"name\" : \"sample-bucket1\",\n" +
           "        \"bucketName\" : \"sample-bucket1\"\n" +
           "    },\n" +
           "    {\n" +
           "        \"type\" : \"aws:core:s3Bucket\",\n" +
           "        \"name\" : \"sample-bucket2\",\n" +
           "        \"bucketName\" : \"sample-bucket2\"\n" +
           "    }\n" +
           "    ]\n" +
           "}\n")
    void createResourcesProject() {
        Assertions.assertEquals(0, new Kernel().execute(args));
    }

    @Test
    @StdIo("{\n" +
           "  \"project\": {\n" +
           "    \"name\": \"TestProject\",\n" +
           "    \"version\": \"20230101-00\"\n" +
           "  },\n" +
           "  \"placement\": {\n" +
           "    \"stage\": \"prod\",\n" +
           "    \"provider\": \"aws\",\n" +
           "    \"account\": \"abc123\",\n" +
           "    \"region\": \"us-east-2\"\n" +
           "  },\n" +
           "  \"arcs\": [\n" +
           "    {\n" +
           "      \"type\": \"aws:core:s3CopyArc\",\n" +
           "      \"name\": \"copy\",\n" +
           "      \"sources\": {\n" +
           "        \"main\": {\n" +
           "          \"name\": \"ingress\",\n" +
           "          \"version\": \"20220101\",\n" +
           "          \"pathURI\": \"s3://clusterless-test/ingress/\"\n" +
           "        }\n" +
           "      },\n" +
           "      \"sinks\": {\n" +
           "        \"main\": {\n" +
           "          \"name\": \"copy\",\n" +
           "          \"version\": \"20230101\",\n" +
           "          \"pathURI\": \"s3://clusterless-test/copy/\"\n" +
           "        }\n" +
           "      }\n" +
           "    }\n" +
           "  ]\n" +
           "}\n" +
           "          ")
    void copyArcProject() {
        Assertions.assertEquals(0, new Kernel().execute(args));
    }
}
