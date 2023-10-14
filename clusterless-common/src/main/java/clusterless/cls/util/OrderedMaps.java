/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Is a duplicate of {@link Map#of()} but the order is retained.
 */
public class OrderedMaps {
    public static <K, V> Map<K, V> of(K k1, V v1) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
                put(k3, v3);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
                put(k3, v3);
                put(k4, v4);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
                put(k3, v3);
                put(k4, v4);
                put(k5, v5);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
                put(k3, v3);
                put(k4, v4);
                put(k5, v5);
                put(k6, v6);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
                put(k3, v3);
                put(k4, v4);
                put(k5, v5);
                put(k6, v6);
                put(k7, v7);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
                put(k3, v3);
                put(k4, v4);
                put(k5, v5);
                put(k6, v6);
                put(k7, v7);
                put(k8, v8);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
                put(k3, v3);
                put(k4, v4);
                put(k5, v5);
                put(k6, v6);
                put(k7, v7);
                put(k8, v8);
                put(k9, v9);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
                put(k3, v3);
                put(k4, v4);
                put(k5, v5);
                put(k6, v6);
                put(k7, v7);
                put(k8, v8);
                put(k9, v9);
                put(k10, v10);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                      K k11, V v11) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
                put(k3, v3);
                put(k4, v4);
                put(k5, v5);
                put(k6, v6);
                put(k7, v7);
                put(k8, v8);
                put(k9, v9);
                put(k10, v10);
                put(k11, v11);
            }
        };
    }

    public static <K, V> Map<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5,
                                      K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10,
                                      K k11, V v11, K k12, V v12) {
        return new LinkedHashMap<>() {
            {
                put(k1, v1);
                put(k2, v2);
                put(k3, v3);
                put(k4, v4);
                put(k5, v5);
                put(k6, v6);
                put(k7, v7);
                put(k8, v8);
                put(k9, v9);
                put(k10, v10);
                put(k11, v11);
                put(k12, v12);
            }
        };
    }
}
