/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Is a duplicate of {@link List#of()} but no null values will be added.
 */
public class SafeList {
    private static <V> void safePut(List<V> list, V v) {
        Optional.ofNullable(v).map(OrderedSafeMaps::nullIfEmpty).ifPresent(list::add);
    }

    public static <E> List<E> of(E e1) {
        return new ArrayList<>() {
            {
                safePut(this, e1);
            }
        };
    }

    public static <E> List<E> of(E e1, E e2) {
        return new ArrayList<>() {
            {
                safePut(this, e1);
                safePut(this, e2);
            }
        };
    }

    public static <E> List<E> of(E e1, E e2, E e3) {
        return new ArrayList<>() {
            {
                safePut(this, e1);
                safePut(this, e2);
                safePut(this, e3);
            }
        };
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4) {
        return new ArrayList<>() {
            {
                safePut(this, e1);
                safePut(this, e2);
                safePut(this, e3);
                safePut(this, e4);
            }
        };
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5) {
        return new ArrayList<>() {
            {
                safePut(this, e1);
                safePut(this, e2);
                safePut(this, e3);
                safePut(this, e4);
                safePut(this, e5);
            }
        };
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6) {
        return new ArrayList<>() {
            {
                safePut(this, e1);
                safePut(this, e2);
                safePut(this, e3);
                safePut(this, e4);
                safePut(this, e5);
                safePut(this, e6);
            }
        };
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7) {
        return new ArrayList<>() {
            {
                safePut(this, e1);
                safePut(this, e2);
                safePut(this, e3);
                safePut(this, e4);
                safePut(this, e5);
                safePut(this, e6);
                safePut(this, e7);
            }
        };
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8) {
        return new ArrayList<>() {
            {
                safePut(this, e1);
                safePut(this, e2);
                safePut(this, e3);
                safePut(this, e4);
                safePut(this, e5);
                safePut(this, e6);
                safePut(this, e7);
                safePut(this, e8);
            }
        };
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9) {
        return new ArrayList<>() {
            {
                safePut(this, e1);
                safePut(this, e2);
                safePut(this, e3);
                safePut(this, e4);
                safePut(this, e5);
                safePut(this, e6);
                safePut(this, e7);
                safePut(this, e8);
                safePut(this, e9);
            }
        };
    }

    public static <E> List<E> of(E e1, E e2, E e3, E e4, E e5, E e6, E e7, E e8, E e9, E e10) {
        return new ArrayList<>() {
            {
                safePut(this, e1);
                safePut(this, e2);
                safePut(this, e3);
                safePut(this, e4);
                safePut(this, e5);
                safePut(this, e6);
                safePut(this, e7);
                safePut(this, e8);
                safePut(this, e9);
                safePut(this, e10);
            }
        };
    }
}
