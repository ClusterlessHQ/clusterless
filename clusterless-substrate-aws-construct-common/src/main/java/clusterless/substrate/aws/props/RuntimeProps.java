/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.props;

import clusterless.model.Struct;

public class RuntimeProps implements Struct {
    public static int requireValidMemorySizeMib(int memorySizeMiB, int memLower, int memUpper) {
        if (!validMemorySizeMib(memorySizeMiB, memLower, memUpper)) {
            throw new IllegalArgumentException("invalid memory size, must between: " + memLower + " and " + memUpper + ", got: " + memorySizeMiB);
        }

        return memorySizeMiB;
    }

    public static boolean validMemorySizeMib(int memorySizeMiB, int memLower, int memUpper) {
        return memLower <= memorySizeMiB && memorySizeMiB <= memUpper;
    }
}
