/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.startup;

import clusterless.printer.Printer;
import picocli.CommandLine;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class Startup {
    public static final String CLUSTERLESS_HOME = "clusterless.home";

    static {
        // System.getenv().forEach((k, v) -> System.out.println(k + " = " + v));

        setHome();
    }

    protected static void setHome() {
        try {
            String jarPath = Startup.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            Path resolved = Paths.get(jarPath).getParent();

            if (jarPath.endsWith(".jar")) {
                resolved = resolved.getParent();
            }

            System.setProperty(CLUSTERLESS_HOME, resolved.toAbsolutePath().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @CommandLine.Mixin
    Verbosity verbosity = new Verbosity();

    @CommandLine.Mixin
    Printer printer = new Printer();

    public Printer printer() {
        return printer;
    }
}
