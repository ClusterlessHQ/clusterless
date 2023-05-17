/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.naming;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Label simplifies creating complex strings used for naming, displays, and paths.
 * <p>
 * An Enum can be a Label by implementing {@link EnumPartition}.
 */
public interface Partition {
    Pattern SEPARATOR_REGEX = Pattern.compile("^/+$");

    /**
     * Concatenate all the given labels .
     *
     * @param partitions the labels to concatenate
     * @return a concatenated Label instance
     */
    static Partition concat(Partition... partitions) {
        return Arrays.stream(partitions).reduce(Partition.NULL, Partition::with);
    }

    /**
     * Will force the enum type and value to be lowercase, unless the enum also
     * implements {@link Label}, in which {@link Label#lowerHyphen()} is called.
     */
    interface EnumPartition extends NamedPartition {
        String name();

        default String partition() {
            return String.format("%s=%s", key(), value());
        }

        default String key() {
            return getClass().getSimpleName().toLowerCase(Locale.ROOT);
        }

        default String value() {
            // todo: currently an enum can't implement both
            if (this instanceof Label) {
                return ((Label) this).lowerHyphen();
            }
            return name().toLowerCase(Locale.ROOT);
        }
    }

    Partition NULL = new Partition() {
        @Override
        public String partition() {
            return null;
        }

        @Override
        public boolean isNull() {
            return true;
        }
    };

    Partition SEPARATOR = new Partition() {
        @Override
        public String partition() {
            return "/";
        }

        @Override
        public boolean isSeparator() {
            return true;
        }
    };

    static String nameOrNull(Partition value) {
        return value == null ? null : value.partition();
    }

    static Partition namedOf(Object key, Object value) {
        Partition keyPart = of(key);
        Partition valuePart = of(value);

        if (valuePart.isNull()) {
            return NULL;
        }

        return keyPart.named(valuePart);
    }

    interface NamedPartition extends Partition {
    }

    static Partition literal(String value) {
        Partition of = of(value);

        return new Partition() {
            @Override
            public String partition() {
                return of.partition();
            }

            @Override
            public boolean isLiteral() {
                return true;
            }
        };
    }

    static Partition of(Object value) {
        if (value == null) {
            return NULL;
        }

        if (value instanceof String) {
            return of((String) value);
        }

        if (value instanceof Partition) {
            return of((Partition) value);
        }

        if (value instanceof Optional) {
            return of(((Optional<?>) value).orElse(null));
        }

        return of(value.toString());
    }

    private static Partition of(Partition value) {
        if (value == null) {
            return NULL;
        }

        return value;
    }

    private static Partition of(String value) {
        if (value == null) {
            return NULL;
        }

        if (SEPARATOR_REGEX.matcher(value).matches()) {
            return SEPARATOR;
        }

        return () -> value;
    }

    default boolean isNull() {
        return partition() == null;
    }

    default boolean isSeparator() {
        return false;
    }

    default boolean isLiteral() {
        return false;
    }

    default Partition having(String... values) {
        return Arrays.stream(values)
                .map(Partition::of)
                .reduce(this, Partition::with);
    }

    default Partition withNamedTerminal(Object key, Object value) {
        Partition keyPart = of(key);
        Partition valuePart = of(value);

        if (valuePart.isNull()) {
            return this.withTerminal(null);
        }

        return with(keyPart.named(valuePart));
    }

    default Partition withNamed(Object key, Object value) {
        Partition keyPart = of(key);
        Partition valuePart = of(value);

        if (valuePart.isNull()) {
            return this;
        }

        return with(keyPart.named(valuePart));
    }

    default NamedPartition named(Partition value) {
        return () -> String.format("%s=%s", partition(), value.partition());
    }

    default Partition withTerminal(Object object) {
        if (object == null) {
            return new Partition() {
                @Override
                public Partition with(Object object) {
                    return Partition.super.with(null);
                }

                @Override
                public String partition() {
                    return Partition.this.partition();
                }
            };
        }

        return this.with(object);
    }

    default Partition with(Object object) {
        if (object == null) {
            return this;
        }

        if (object instanceof Optional) {
            return with(((Optional<?>) object).orElse(null));
        }

        if (object instanceof Label) {
            return with(((Label) object).lowerHyphenPath());
        }

        if (!(object instanceof Partition)) {
            return with(Partition.of(object.toString()));
        }

        Partition partition = (Partition) object;

        if (partition.isNull()) {
            return this;
        }

        // if first in chain is already null, return the next
        if (this.isNull()) {
            return partition;
        }


        // collapse the two slashes
        if (this.isSeparator() && partition.isSeparator()) {
            return NULL;
        }

        // drop the separator
        if (partition.isSeparator()) {
            return this;
        }

        return () -> partition.isLiteral() ?
                String.format("%s%s", Partition.this.partition(), partition.partition()) :
                String.format("%s/%s", Partition.this.partition(), partition.partition());
    }

    default Partition thisIfNull(Partition partition) {
        if (partition == null || partition.isNull()) {
            return this;
        }

        return partition;
    }

    /**
     * Results in a string, with no leading or trailing slash
     *
     * @return String year=2023/month=12
     */
    String partition();

    /**
     * Results in a string, with no leading but optional trailing slash
     *
     * @return String year=2023/month=12
     */
    default String partition(boolean trailingSlash) {
        return !isNull() && trailingSlash ? partition().concat("/") : partition();
    }

    /**
     * Results in a path string, with a leading and trailing slash
     * <p>
     * Unless object is not null, then results in a prefix string, with only a leading
     *
     * @return String /year=2023/month=12/ or /year=2023/month=12/file.txt
     */
    default String pathUnless(Object object) {
        Partition of = of(object);
        if (of.isNull()) {
            return path();
        }

        return with(of).prefix();
    }

    /**
     * Results in a path string, with a leading and trailing slash
     *
     * @return String /year=2023/month=12/
     */
    default String path() {
        return "/".concat(partition(true));
    }

    /**
     * Results in a prefix string, with only a leading
     *
     * @return String /year=2023/month=12
     */
    default String prefix() {
        return "/".concat(partition(false));
    }

    default int compareTo(Partition o) {
        return Objects.compare(partition(), o.partition(), String::compareTo);
    }
}
