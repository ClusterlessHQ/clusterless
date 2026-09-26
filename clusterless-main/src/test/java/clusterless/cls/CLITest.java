/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Runs {@code cls} through the real {@link Main#main(String[])} entry in a child JVM and checks
 * stdout, stderr, and the exit code. A child JVM gives each run its own logback configuration and
 * its own one-time component service loading, which only logs on first class initialization.
 * <p>
 * stdout carries data only; log lines go to stderr so {@code -v} never corrupts command output.
 */
class CLITest {
    private static final Pattern LOG_LINE = Pattern.compile("INFO|DEBUG|\\d\\d:\\d\\d:\\d\\d\\.\\d{3}");
    private static final String COMPONENT_LOG = "loading component service provider";

    @TempDir
    Path tempDir;

    record Result(int exitCode, String out, String err) {
    }

    @Test
    void verboseLogsGoToStderrNotStdout() throws Exception {
        Result result = cls("show", "component", "--list", "-v");

        assertEquals(0, result.exitCode(), result.err());
        assertTrue(result.out().contains("aws:core:s3Bucket"), result.out());
        assertFalse(LOG_LINE.matcher(result.out()).find(), result.out());
        assertTrue(result.err().contains(COMPONENT_LOG), result.err());
    }

    @Test
    void defaultVerbosityLogsNothing() throws Exception {
        Result quiet = cls("show", "component", "--list");
        Result verbose = cls("show", "component", "--list", "-v");

        assertEquals(0, quiet.exitCode(), quiet.err());
        assertEquals("", quiet.err());
        assertEquals(verbose.out(), quiet.out());
    }

    @Test
    void debugLogsGoToStderrNotStdout() throws Exception {
        Result result = cls("show", "component", "--list", "-vv");

        assertEquals(0, result.exitCode(), result.err());
        assertFalse(LOG_LINE.matcher(result.out()).find(), result.out());
        assertTrue(result.err().contains(COMPONENT_LOG), result.err());
    }

    private Result cls(String... args) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
        command.add(Main.class.getName());
        command.addAll(List.of(args));

        File out = Files.createTempFile(tempDir, "stdout", ".txt").toFile();
        File err = Files.createTempFile(tempDir, "stderr", ".txt").toFile();

        ProcessBuilder builder = new ProcessBuilder(command)
                .redirectOutput(out)
                .redirectError(err);

        // the classpath can exceed the command line limit, the child reads it from the environment
        builder.environment().put("CLASSPATH", System.getProperty("java.class.path"));

        Process process = builder.start();

        if (!process.waitFor(2, TimeUnit.MINUTES)) {
            process.destroyForcibly();
            fail("cls " + String.join(" ", args) + " did not exit");
        }

        return new Result(process.exitValue(), Files.readString(out.toPath()), Files.readString(err.toPath()));
    }
}
