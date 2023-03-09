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
 * An Enum can be a Label by implementing {@link EnumPartition}.
 */
public interface Partition {
    /**
     * Concatenate all the given labels .
     *
     * @param partitions the labels to concatenate
     * @return a concatenated Label instance
     */
    static Partition concat(Partition... partitions) {
        return Arrays.stream(partitions).reduce(Partition.NULL, Partition::with);
    }

    interface EnumPartition extends NamedPartition {
        String name();

        default String partition() {
            return String.format("%s=%s", key(), value());
        }

        default String key() {
            return getClass().getSimpleName().toLowerCase(Locale.ROOT);
        }

        default String value() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    Partition NULL = () -> null;

    static String nameOrNull(Partition value) {
        return value == null ? null : value.partition();
    }

    static Partition namedOf(Object key, Object value) {
        Partition keyPart = of(key);
        Partition valuePart = of(value);

        return keyPart.named(valuePart);
    }

    interface NamedPartition extends Partition {
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


        return () -> value.toLowerCase(Locale.ROOT);
    }

    default boolean isNull() {
        return partition() == null;
    }

    default Partition having(String... values) {
        return Arrays.stream(values)
                .map(Partition::of)
                .reduce(this, Partition::with);
    }

    default Partition withNamed(Object key, Object value) {
        Partition keyPart = of(key);
        Partition valuePart = of(value);

        return with(keyPart.named(valuePart));
    }

    default NamedPartition named(Partition value) {
        if (value == null || value.isNull()) {
            return this::partition;
        }

        return () -> String.format("%s=%s", partition(), value.partition());
    }

    default Partition with(Object object) {
        if (object == null) {
            return this;
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

        // if first in chain in already null, return the next
        if (this.isNull()) {
            return partition;
        }

        return () -> String.format("%s/%s", Partition.this.partition(), partition.partition());
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
        return trailingSlash ? partition().concat("/") : partition();
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