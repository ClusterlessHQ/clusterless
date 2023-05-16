/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class Lazy<T> implements Supplier<T>, Serializable {
    // read http://javarevisited.blogspot.de/2014/05/double-checked-locking-on-singleton-in-java.html
    private transient volatile Supplier<? extends T> supplier;

    // will behave as a volatile in reality, because a supplier volatile read will update all fields (see https://www.cs.umd.edu/~pugh/java/memoryModel/jsr-133-faq.html#volatile)
    private T value;

    private Lazy(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");

        if (supplier instanceof Lazy) {
            return (Lazy<T>) supplier;
        } else {
            return new Lazy<>(supplier);
        }
    }

    public T get() {
        return (supplier == null) ? value : computeValue();
    }

    private synchronized T computeValue() {
        final Supplier<? extends T> s = supplier;
        if (s != null) {
            value = s.get();
            supplier = null;
        }
        return value;
    }
}
