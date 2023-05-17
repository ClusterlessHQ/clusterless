/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.props;

import clusterless.model.Struct;

public class BatchRuntimeProps implements Struct {
    public static final int MEM_DEFAULT = Memory.MEM_1_024MB;
    public static final int MEM_LOWER = Memory.MEM_512MB;
    public static final int MEM_UPPER = Memory.MEM_10_240GiB;

    private int memorySizeMB = MEM_DEFAULT;

    private int retryAttempts = 1;

    private int timeoutMin = 60;

    private Architecture architecture = Architecture.X86_64;

    public int memorySizeMB() {
        return memorySizeMB;
    }

    public BatchRuntimeProps setMemorySizeMB(int memorySizeMB) {
        this.memorySizeMB = memorySizeMB;
        return this;
    }

    public int retryAttempts() {
        return retryAttempts;
    }

    public BatchRuntimeProps setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
        return this;
    }

    public int timeoutMin() {
        return timeoutMin;
    }

    public BatchRuntimeProps setTimeoutMin(int timeoutMin) {
        this.timeoutMin = timeoutMin;
        return this;
    }

    public Architecture architecture() {
        return architecture;
    }

    public BatchRuntimeProps setArchitecture(Architecture architecture) {
        this.architecture = architecture;
        return this;
    }
}
