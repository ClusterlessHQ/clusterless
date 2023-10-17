/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.construct;

import clusterless.cls.naming.Label;
import clusterless.cls.substrate.aws.scoped.ScopedConstruct;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.logs.LogGroup;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

public class LambdaLogGroupConstruct extends ScopedConstruct {

    public LambdaLogGroupConstruct(@NotNull Construct scope, Label id, Function function) {
        this(scope, id, function, RetentionDays.ONE_DAY, RemovalPolicy.DESTROY);
    }

    public LambdaLogGroupConstruct(@NotNull Construct scope, @NotNull Label id, Function function, RetentionDays retentionDays, RemovalPolicy removalPolicy) {
        super(scope, Label.of("LambdaLogGroup").with(id).camelCase());

        LogGroup.Builder.create(this, Label.of("LogGroup").with(id).camelCase())
                .logGroupName("/aws/lambda/" + function.getFunctionName())
                .removalPolicy(removalPolicy)
                .retention(retentionDays)
                .build();
    }
}
