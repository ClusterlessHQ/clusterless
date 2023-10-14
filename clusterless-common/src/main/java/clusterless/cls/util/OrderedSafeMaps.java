/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Is a duplicate of {@link Map#of()} but the order is retained.
 * <p>
 * Also an entry will not be added if the value is null.
 */
public class OrderedSafeMaps {

    public static <I, V> V ifPresent(I intermediate, Function<I, V> function) {
        return ifPresent(intermediate, function, null);
    }

    public static <I, V> V ifPresent(I intermediate, Function<I, V> function, V other) {
        return Optional.ofNullable(intermediate).map(function).orElse(other);
    }

    private static <K, V> void safePut(Map<K, V> map, K k, V v) {
        Optional.ofNullable(v).map(OrderedSafeMaps::nullIfEmpty).ifPresent(p -> map.put(k, p));
    }

    static <V> V nullIfEmpty(V v) {
        if (v instanceof Collection<?>) {
            return ((Collection<?>) v).isEmpty() ? null : v;
        }

        if (v instanceof Map<?, ?>) {
            return ((Map<?, ?>) v).isEmpty() ? null : v;
        }

        return v;
    }

    public static <K, V> Map<K, V> of(K k1, V v1) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
                safePut(this, k5, v5);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
                safePut(this, k5, v5);
                safePut(this, k6, v6);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
                safePut(this, k5, v5);
                safePut(this, k6, v6);
                safePut(this, k7, v7);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
                safePut(this, k5, v5);
                safePut(this, k6, v6);
                safePut(this, k7, v7);
                safePut(this, k8, v8);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
                safePut(this, k5, v5);
                safePut(this, k6, v6);
                safePut(this, k7, v7);
                safePut(this, k8, v8);
                safePut(this, k9, v9);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
                safePut(this, k5, v5);
                safePut(this, k6, v6);
                safePut(this, k7, v7);
                safePut(this, k8, v8);
                safePut(this, k9, v9);
                safePut(this, k10, v10);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                      K k11, V v11) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
                safePut(this, k5, v5);
                safePut(this, k6, v6);
                safePut(this, k7, v7);
                safePut(this, k8, v8);
                safePut(this, k9, v9);
                safePut(this, k10, v10);
                safePut(this, k11, v11);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                      K k11, V v11, K k12, V v12) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
                safePut(this, k5, v5);
                safePut(this, k6, v6);
                safePut(this, k7, v7);
                safePut(this, k8, v8);
                safePut(this, k9, v9);
                safePut(this, k10, v10);
                safePut(this, k11, v11);
                safePut(this, k12, v12);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                      K k11, V v11, K k12, V v12, K k13, V v13) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
                safePut(this, k5, v5);
                safePut(this, k6, v6);
                safePut(this, k7, v7);
                safePut(this, k8, v8);
                safePut(this, k9, v9);
                safePut(this, k10, v10);
                safePut(this, k11, v11);
                safePut(this, k12, v12);
                safePut(this, k13, v13);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                      K k11, V v11, K k12, V v12, K k13, V v13, K k14, V v14) {
        return new LinkedHashMap<>() {
            {
                safePut(this, k1, v1);
                safePut(this, k2, v2);
                safePut(this, k3, v3);
                safePut(this, k4, v4);
                safePut(this, k5, v5);
                safePut(this, k6, v6);
                safePut(this, k7, v7);
                safePut(this, k8, v8);
                safePut(this, k9, v9);
                safePut(this, k10, v10);
                safePut(this, k11, v11);
                safePut(this, k12, v12);
                safePut(this, k13, v13);
                safePut(this, k14, v14);
            }
        };
    }
}
