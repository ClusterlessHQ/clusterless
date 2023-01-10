/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed;

import clusterless.util.Strings;

import java.util.Objects;

/**
 * Named simplifies creating complex names.
 */
public interface Named {
    interface EnumNamed extends Named {
        String name();

        default String camelCase() {
            return name();
        }
    }

    Named NULL = () -> null;

    static String nameOrNull(Named value) {
        return value == null ? null : value.camelCase();
    }

    static Named of(Named value) {
        if (value == null) {
            return NULL;
        }

        return value;
    }

    static Named of(Named value, Named abbr) {
        if (value == null) {
            return NULL;
        }

        if (abbr == null || abbr.isNull()) {
            return value;
        }

        return value.abbreviated(abbr);
    }

    static Named of(String full, String abbr) {
        return of(of(full), of(abbr));
    }

    default Named abbreviated(Named abbr) {
        return new Named() {
            @Override
            public String camelCase() {
                return Named.this.camelCase();
            }

            @Override
            public Named abbreviated() {
                return abbr;
            }
        };
    }

    static Named of(String value) {
        if (value == null) {
            return NULL;
        }

        if (value.contains("-")) {
            return fromLowerHyphen(value);
        }

        if (value.contains("_")) {
            return fromLowerUnderscore(value);
        }

        return () -> Strings.upperCamel(value);
    }

    static Named fromLowerHyphen(String value) {
        return () -> Strings.lowerHyphenToUpperCamel(value);
    }

    static Named fromLowerUnderscore(String value) {
        return () -> Strings.lowerUnderscoreToCamelCase(value);
    }

    default boolean isNull() {
        return camelCase() == null;
    }

    default Named with(Named named) {
        if (named == null || named.isNull()) {
            return this;
        }

        // if first in chain in already null, return the next
        if (this.isNull()) {
            return named;
        }

        return new Named() {
            @Override
            public String camelCase() {
                return String.format("%s%s", Named.this.camelCase(), named.camelCase());
            }

            @Override
            public String lowerHyphen() {
                return String.format("%s-%s", Named.this.lowerHyphen(), named.lowerHyphen());
            }

            @Override
            public String lowerUnderscore() {
                return String.format("%s_%s", Named.this.lowerUnderscore(), named.lowerUnderscore());
            }

            @Override
            public String shortCamelCase() {
                return String.format("%s%s", Named.this.shortCamelCase(), named.shortCamelCase());
            }

            @Override
            public String shortLowerHyphen() {
                return String.format("%s-%s", Named.this.shortLowerHyphen(), named.shortLowerHyphen());
            }

            @Override
            public String shortLowerUnderscore() {
                return String.format("%s_%s", Named.this.shortLowerUnderscore(), named.shortLowerUnderscore());
            }
        };
    }

    default Named thisIfNull(Named named) {
        if (named == null || named.isNull()) {
            return this;
        }

        return named;
    }

    String camelCase();

    default Named abbreviated() {
        return Named.this::camelCase;
    }

    /**
     * Should always be a short version of Camel Case
     *
     * @return
     */
    default String shortCamelCase() {
        return abbreviated().camelCase();
    }

    default String lowerHyphen() {
        return Strings.camelToLowerHyphen(camelCase());
    }

    default String lowerUnderscore() {
        return Strings.camelToLowerUnderscore(camelCase());
    }

    default String shortLowerHyphen() {
        return Strings.camelToLowerHyphen(shortCamelCase());
    }

    default String shortLowerUnderscore() {
        return Strings.camelToLowerUnderscore(shortCamelCase());
    }

    default int compareTo(Named o) {
        return Objects.compare(camelCase(), o.camelCase(), String::compareTo);
    }
}