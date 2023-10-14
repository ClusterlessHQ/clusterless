/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import java.util.Optional;

public class Optionals {
    public static <T> Optional<T> optional(int index, T[] array) {
        if (index < 0 || index > array.length - 1) {
            return Optional.empty();
        }

        return Optional.ofNullable(array[index]);
    }
}
