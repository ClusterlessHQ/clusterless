/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.props;

public interface Memory {
    int MEM_128MB = 128;
    int MEM_512MB = 512;
    int MEM_1_024MB = 1_024;
    int MEM_2_048MB = 2_048;
    int MEM_5_120MB = 5_120;
    int MEM_10_240GiB = 10_240; // 128 * 80
}
