/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import clusterless.json.JSONUtil;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Helper utility that translates environment properties from or into an object.
 * <p>
 * Currently, the object is transformed from/to json, but this may be problematic long term.
 * <p>
 * One option is to translate from/to a flat map where each value is a unique env var having a single string value.
 * <p>
 * This option may be easier to debug
 */
public class Env {
    public static Map<String, String> toEnv(Object object) {
        return OrderedSafeMaps.of(
                Label.of(object.getClass().getSimpleName()).upperUnderscore(), JSONUtil.writeAsString(object)
        );
    }

    public static <T> T fromEnv(Class<T> type) {
        return fromEnv(type, () -> null);
    }

    public static <T> T fromEnv(Class<T> type, Supplier<T> defaultValue) {
        String value = System.getenv(Label.of(type.getSimpleName()).upperUnderscore());

        if (value == null) {
            return defaultValue.get();
        }

        return JSONUtil.readObject(value, type);
    }
}
