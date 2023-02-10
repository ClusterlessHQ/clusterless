/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

/**
 * Label simplifies creating complex strings used for naming, displays, and paths.
 * <p>
 * An Enum can be a Label by implementing {@link EnumLabel}.
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

    static Label of(Object value) {
        if (value == null) {
            return NULL;
        }

        if (value instanceof String) {
            return of((String) value);
        }

        if (value instanceof Label) {
            return of((Label) value);
        }

        return of(value.toString());
    }

    private static Label of(Label value) {
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

    default Label upperOnly() {
        return new Label() {
            @Override
            public String camelCase() {
                return Label.this.camelCase() != null ? Label.this.camelCase().toUpperCase(Locale.ROOT) : null;
            }

            @Override
            public String lowerHyphen() {
                return this.camelCase();
            }

            @Override
            public String lowerHyphenPath() {
                return this.camelCase();
            }

            @Override
            public String lowerUnderscore() {
                return this.camelCase();
            }

            @Override
            public String upperUnderscore() {
                return this.camelCase();
            }

            @Override
            public String shortLowerHyphen() {
                return this.camelCase();
            }

            @Override
            public String shortLowerUnderscore() {
                return this.camelCase();
            }
        };
    }

    private static Label of(String value) {
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

    default Label having(String... values) {
        return Arrays.stream(values)
                .map(Label::of)
                .reduce(this, Label::with);
    }

    default Label with(Object object) {
        if (object == null) {
            return this;
        }

        if (!(object instanceof Label label)) {
            return with(Label.of(object.toString()));
        }

        if (label.isNull()) {
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
            public String lowerHyphenPath() {
                return String.format("%s/%s", Label.this.lowerHyphenPath(), label.lowerHyphenPath());
            }

            @Override
            public String lowerUnderscore() {
                return String.format("%s_%s", Label.this.lowerUnderscore(), label.lowerUnderscore());
            }

            @Override
            public String upperUnderscore() {
                return String.format("%s_%s", Label.this.upperUnderscore(), label.upperUnderscore());
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

    default String lowerHyphenPath() {
        return Strings.camelToLowerHyphen(camelCase());
    }

    default String lowerHyphenPath(boolean trailingSlash) {
        return trailingSlash ? lowerHyphenPath().concat("/") : lowerHyphenPath();
    }

    default String lowerUnderscore() {
        return Strings.camelToLowerUnderscore(camelCase());
    }

    default String upperUnderscore() {
        return Strings.camelToUpperUnderscore(camelCase());
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