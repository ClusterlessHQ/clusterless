/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class Runtimes {
    public static String choose(String mac, String win, String linux) {

        switch (current()) {

            case mac:
                return mac;
            case win:
                return win;
            case linux:
                return linux;
        }

        throw new IllegalStateException("unknown runtime: " + current());
    }

    public enum Runtime {
        mac,
        win,
        linux
    }

    public static Runtime current() {
        return isWindows() ? Runtime.win : isMacOS() ? Runtime.mac : Runtime.linux;
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static boolean isMacOS() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static Optional<Path> findExecutable(String name) {
        String path = System.getenv("PATH");

        String[] split = path.split(File.pathSeparator);

        return Arrays.stream(split)
                .map(s -> Paths.get(s).resolve(name))
                .filter(Files::exists).findFirst();
    }

    public static String getHome(Class<?> type) {
        try {
            String jarPath = type
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            Path resolved = Paths.get(jarPath).getParent();

            if (jarPath.endsWith(".jar")) {
                resolved = resolved.getParent();
            }

            return resolved.toAbsolutePath().toString();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
