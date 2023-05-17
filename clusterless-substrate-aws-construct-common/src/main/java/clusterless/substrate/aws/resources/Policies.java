/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import software.amazon.awscdk.services.iam.Effect;
import software.amazon.awscdk.services.iam.PolicyStatement;

import java.util.List;

public class Policies {
    public static PolicyStatement createCloudWatchPolicyStatement() {
        return PolicyStatement.Builder.create()
                .sid("CloudWatchPolicy")
                .actions(List.of(
                        "cloudwatch:PutMetricData",
                        "cloudwatch:PutMetricAlarm",
                        "cloudwatch:SetAlarmState"
                ))
                .resources(List.of("*"))
                .effect(Effect.ALLOW)
                .build();
    }
}
