/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 *
 */
public class LogUtil {
    public static void disable() {
        setLoggingLevel(Level.OFF);
    }

    public static void debug() {
        setLoggingLevel(Level.DEBUG);
    }

    public static void info() {
        setLoggingLevel(Level.INFO);
    }

    public static void setLoggingLevel(Level level) {
        // a hack, but works
        ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).setLevel(level);
    }
}
