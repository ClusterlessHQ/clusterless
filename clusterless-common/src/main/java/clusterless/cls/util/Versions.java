/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Versions {
    public static final String WIP = "1.0-wip-dev";

    public static String clsVersion() {
        Properties properties = new Properties();

        try {
            properties.load(resourceAsStream());
        } catch (IOException e) {
            return WIP;
        }

        return properties.getProperty("clusterless.release.full", WIP);
    }

    @Nullable
    private static InputStream resourceAsStream() {
        return Versions.class.getClassLoader().getResourceAsStream("version.properties");
    }
}
