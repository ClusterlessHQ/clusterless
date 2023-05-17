/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.props;

import software.amazon.awscdk.services.ecr.assets.Platform;
import software.amazon.awscdk.services.lambda.Architecture;

public class Lookup {
    public static Architecture architecture(clusterless.substrate.aws.props.Architecture architecture) {
        switch (architecture) {
            case ARM_64: {
                return Architecture.ARM_64;
            }
            case X86_64: {
                return Architecture.X86_64;
            }
        }
        throw new IllegalStateException("unknown architecture: " + architecture);
    }

    public static Platform platform(clusterless.substrate.aws.props.Architecture architecture) {
        switch (architecture) {
            case ARM_64: {
                return Platform.LINUX_ARM64;
            }
            case X86_64: {
                return Platform.LINUX_AMD64;
            }
        }
        throw new IllegalStateException("unknown platform: " + architecture);
    }
}
