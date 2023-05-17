/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import clusterless.json.JSONUtil;
import clusterless.naming.Fixed;
import clusterless.naming.Label;

import java.util.Map;
import java.util.function.Function;
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
    public static final Label PREFIX_CLS = Fixed.of("CLS");
    protected static final Label POSTFIX_JAVA = Fixed.of("JAVA");
    protected static final Label POSTFIX_JSON = Fixed.of("JSON");

    public static Map<String, String> toEnv(Object object) {
        return OrderedSafeMaps.of(
                keyJSON(object), value(object),
                keyTyped(object), valueTyped(object)
        );
    }

    public static String value(Object object) {
        return JSONUtil.writeAsStringSafe(object);
    }

    public static String valueTyped(Object object) {
        return JSONUtil.writeTypedAsStringSafe(object);
    }

    public static String keyTyped(Object object) {
        return key(object, POSTFIX_JAVA);
    }

    public static String key(Object object) {
        return key(object, null);
    }

    public static String keyJSON(Object object) {
        return key(object, POSTFIX_JSON);
    }

    public static String key(Object object, Label postfix) {
        Class<?> type = object.getClass();
        return keyNameFor(type, postfix);
    }

    public static <T> T fromEnv(Class<T> type) {
        return fromEnv(type, () -> null);
    }

    public static <T> T fromEnv(Class<T> type, Supplier<T> defaultValue) {
        Function<String, String> envMap = System::getenv;
        return fromEnv(envMap, type, defaultValue);
    }

    public static <T> T fromEnv(Function<String, String> envMap, Class<T> type, Supplier<T> defaultValue) {
        String key = keyNameFor(type, POSTFIX_JAVA); // need the typed version to load our internal objects
        String value = envMap.apply(key);

        if (value == null) {
            return defaultValue.get();
        }

        return JSONUtil.readTypedObjectSafe(value, type);
    }

    private static String keyNameFor(Class<?> type, Label postfix) {
        return PREFIX_CLS.with(type.getSimpleName()).with(postfix).upperUnderscore();
    }
}
