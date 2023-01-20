/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import com.google.common.collect.Lists;
import picocli.CommandLine;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 */
public abstract class Manage {
    @CommandLine.ParentCommand
    Kernel kernel;

    public Manage() {
    }

    protected Integer executeCDK(String... cdkArgs) {
        List<String> args = Lists.asList(kernel.cdk, cdkArgs);

        return executeProcess(args);
    }

    protected int executeProcess(String... args) {
        return executeProcess(List.of(args));
    }

    protected int executeProcess(List<String> args) {
        try {
            return process(args);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private int process(List<String> args) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(args)
                .inheritIO()
                .start();

        return process.waitFor();
    }
}
