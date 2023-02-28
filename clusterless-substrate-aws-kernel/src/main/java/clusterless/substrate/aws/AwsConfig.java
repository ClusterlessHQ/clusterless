/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.config.Config;
import clusterless.config.Configuration;
import clusterless.util.Label;

/**
 * --require-approval     What security-sensitive changes need manual
 * approval
 * [string] [choices: "never", "any-change", "broadening"]
 */
public class AwsConfig extends Configuration {

    public static class CDK extends Config {
        public enum Approval implements Label.EnumLabel {
            never,
            any_change,
            broadening;

            public String value() {
                return lowerHyphen();
            }
        }

        Approval requireDeployApproval = Approval.broadening;

        boolean requireDestroyApproval = true;

        public Approval requireDeployApproval() {
            return requireDeployApproval;
        }

        public boolean requireDestroyApproval() {
            return requireDestroyApproval;
        }
    }

    CDK cdk = new CDK();

    public AwsConfig() {
    }

    @Override
    public String name() {
        return "aws";
    }

    public CDK cdk() {
        return cdk;
    }
}
