/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.props;

/**
 * https://docs.aws.amazon.com/lambda/latest/operatorguide/computing-power.html
 * 128 MB and 10,240 MB
 */
public class LambdaJavaRuntimeProps extends RuntimeProps {
    public static final int MEM_DEFAULT = Memory.MEM_512MB;
    public static final int MEM_LOWER = Memory.MEM_128MB;
    public static final int MEM_UPPER = Memory.MEM_10_240GiB;

    private int memorySizeMB = MEM_DEFAULT;

    private int retryAttempts = 0;

    private int timeoutMin = 5;

    private Architecture architecture = Architecture.ARM_64;

    public LambdaJavaRuntimeProps() {
    }

    public LambdaJavaRuntimeProps(int memorySizeMB, int retryAttempts, int timeoutMin) {
        this.memorySizeMB = requireValidMemorySizeMib(memorySizeMB, MEM_LOWER, MEM_UPPER);
        this.retryAttempts = retryAttempts;
        this.timeoutMin = timeoutMin;
    }

    public LambdaJavaRuntimeProps(int memorySizeMB, int retryAttempts, int timeoutMin, Architecture architecture) {
        this.memorySizeMB = requireValidMemorySizeMib(memorySizeMB, MEM_LOWER, MEM_UPPER);
        this.retryAttempts = retryAttempts;
        this.timeoutMin = timeoutMin;
        this.architecture = architecture;
    }

    public int memorySizeMB() {
        return memorySizeMB;
    }

    public int retryAttempts() {
        return retryAttempts;
    }

    public int timeoutMin() {
        return timeoutMin;
    }

    public Architecture architecture() {
        return architecture;
    }

    public static Builder builder() {
        return Builder.aLambdaJavaRuntimeProps();
    }

    public static final class Builder {

        int memorySizeMB = MEM_DEFAULT;
        int retryAttempts = 3;
        int timeoutMin = 5;
        Architecture architecture = Architecture.ARM_64;

        private Builder() {
        }

        public static Builder aLambdaJavaRuntimeProps() {
            return new Builder();
        }

        public Builder withMemorySizeMB(int memorySizeMB) {
            this.memorySizeMB = memorySizeMB;
            return this;
        }

        public Builder withRetryAttempts(int retryAttempts) {
            this.retryAttempts = retryAttempts;
            return this;
        }

        public Builder withTimeoutMin(int timeoutMin) {
            this.timeoutMin = timeoutMin;
            return this;
        }

        public Builder withArchitecture(Architecture architecture) {
            this.architecture = architecture;
            return this;
        }

        public LambdaJavaRuntimeProps build() {
            return new LambdaJavaRuntimeProps(memorySizeMB, retryAttempts, timeoutMin, architecture);
        }
    }
}
