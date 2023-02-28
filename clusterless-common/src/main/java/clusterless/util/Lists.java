/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Lists {
    public static <V> List<V> list(Map<V, V> map) {
        List<V> result = new ArrayList<>();

        map.forEach((k, v) -> {
            result.add(k);
            result.add(v);
        });

        return result;
    }

    public static <E extends @Nullable Object> List<E> asList(E first, E[] rest) {
        return com.google.common.collect.Lists.asList(first, rest);
    }

    public static <E extends @Nullable Object> E[] toArray(E first, E[] rest) {
        return asList(first, rest).toArray(rest);
    }

    @SafeVarargs
    public static <V> List<V> concat(List<V> first, List<V>... next) {
        List<V> result = new LinkedList<>(first);

        for (List<V> vs : next) {
            result.addAll(vs);
        }

        return result;
    }
}
