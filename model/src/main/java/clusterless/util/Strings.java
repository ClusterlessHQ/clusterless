/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;


import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 */
public class Strings {
    public static String joinCleanDash(Object... objects) {
        return joinClean("-", objects);
    }

    public static String joinDash(Object... objects) {
        return join("-", objects);
    }

    /**
     * use with {@link #nullOr(Object, Function)}
     *
     * @param separator
     * @param objects
     * @return
     */
    public static String joinClean(String separator, Object... objects) {
        List<String> clean = Arrays.stream(objects)
                .map(o -> Objects.toString(o, null))
                .map(Strings::emptyToNull)
                .collect(Collectors.toList());

        return Joiner.on(separator)
                .skipNulls()
                .join(clean);
    }

    public static String join(String separator, Object... objects) {
        List<String> clean = Arrays.stream(objects)
                .map(o -> Objects.toString(o, null))
                .map(Strings::emptyToNull)
                .collect(Collectors.toList());

        if (clean.stream().anyMatch(Objects::isNull)) {
            return null;
        }

        return Joiner.on(separator)
                .join(clean);
    }

    public static String upperCamel(String string) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, string);
    }

    public static String lowerUnderscoreToCamelCase(String string) {
        if (string == null) {
            return null;
        }

        return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, string);
    }

    public static String camelToLowerHyphen(String string) {
        if (string == null) {
            return null;
        }

        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, string);
    }

    public static String camelToLowerUnderscore(String string) {
        if (string == null) {
            return null;
        }

        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, string);
    }

    public static String lowerHyphenToUpperCamel(String string) {
        if (string == null) {
            return null;
        }

        return CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_CAMEL, string);
    }

    public static String emptyToNull(String string) {
        if (string == null || string.isEmpty()) {
            return null;
        }

        return string;
    }

    public static String nullToEmpty(Object value) {
        if (value == null) {
            return "";
        }

        return value.toString();
    }

    public static <T> String nullOr(T e, Function<T, String> f) {
        if (e == null) {
            return null;
        }

        return f.apply(e);
    }

    public static String removeLast(char delim, String value) {
        if (value != null && value.lastIndexOf(delim) == value.length() - 1) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }

    public static String joinEscaped(Map<String, String> map, String delimiter) {
        return Joiner.on(delimiter)
                .withKeyValueSeparator("=")
                .join(Maps.transformEntries(map, (key, value) -> value == null ? null : String.format("\"%s\"", value)));
    }
}