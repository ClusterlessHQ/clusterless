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
public interface Label {
    interface EnumLabel extends Label {
        String name();

        default String camelCase() {
            return name();
        }
    }

    Label NULL = () -> null;

    static String nameOrNull(Label value) {
        return value == null ? null : value.camelCase();
    }

    static Label of(Label value) {
        if (value == null) {
            return NULL;
        }

        return value;
    }

    static Label of(Label value, Label abbr) {
        if (value == null) {
            return NULL;
        }

        if (abbr == null || abbr.isNull()) {
            return value;
        }

        return value.abbreviated(abbr);
    }

    static Label of(String full, String abbr) {
        return of(of(full), of(abbr));
    }

    default Label abbreviated(Label abbr) {
        return new Label() {
            @Override
            public String camelCase() {
                return Label.this.camelCase();
            }

            @Override
            public Label abbreviated() {
                return abbr;
            }
        };
    }

    static Label of(String value) {
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

    static Label fromLowerHyphen(String value) {
        return () -> Strings.lowerHyphenToUpperCamel(value);
    }

    static Label fromLowerUnderscore(String value) {
        return () -> Strings.lowerUnderscoreToCamelCase(value);
    }

    default boolean isNull() {
        return camelCase() == null;
    }

    default Label with(Label label) {
        if (label == null || label.isNull()) {
            return this;
        }

        // if first in chain in already null, return the next
        if (this.isNull()) {
            return label;
        }

        return new Label() {
            @Override
            public String camelCase() {
                return String.format("%s%s", Label.this.camelCase(), label.camelCase());
            }

            @Override
            public String lowerHyphen() {
                return String.format("%s-%s", Label.this.lowerHyphen(), label.lowerHyphen());
            }

            @Override
            public String lowerUnderscore() {
                return String.format("%s_%s", Label.this.lowerUnderscore(), label.lowerUnderscore());
            }

            @Override
            public String shortCamelCase() {
                return String.format("%s%s", Label.this.shortCamelCase(), label.shortCamelCase());
            }

            @Override
            public String shortLowerHyphen() {
                return String.format("%s-%s", Label.this.shortLowerHyphen(), label.shortLowerHyphen());
            }

            @Override
            public String shortLowerUnderscore() {
                return String.format("%s_%s", Label.this.shortLowerUnderscore(), label.shortLowerUnderscore());
            }
        };
    }

    default Label thisIfNull(Label label) {
        if (label == null || label.isNull()) {
            return this;
        }

        return label;
    }

    String camelCase();

    default Label abbreviated() {
        return Label.this::camelCase;
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

    default int compareTo(Label o) {
        return Objects.compare(camelCase(), o.camelCase(), String::compareTo);
    }
}