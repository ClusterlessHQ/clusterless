/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.startup;

import clusterless.config.CommonConfig;
import clusterless.config.Configurations;
import clusterless.printer.Printer;
import picocli.CommandLine;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 */
public class Startup {
    public static final String CLUSTERLESS_HOME = "clusterless.home";

    static {
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

    public static void asPropertyArgs(List<String> args, Map<String, String> properties) {
        properties.entrySet().stream()
                .map((e) -> e.getKey() + "=" + e.getValue()).
                forEach(p -> args.addAll(List.of("-D", p)));
    }

    @CommandLine.Mixin
    protected Verbosity verbosity = new Verbosity();

    @CommandLine.Mixin
    protected Printer printer = new Printer();

    @CommandLine.Option(
            names = {"-D", "--property"},
            mapFallbackValue = "",
            description = "Optional key=value properties, will be passed down.")
    Properties properties = new Properties();

    Configurations configurations = new Configurations(this::properties);

    public Startup() {
        configurations.add(CommonConfig.configOptions);
    }

    public Properties properties() {
        return properties;
    }

    public Configurations configurations() {
        return configurations;
    }

    public Verbosity verbosity() {
        return verbosity;
    }

    public Printer printer() {
        return printer;
    }
}
