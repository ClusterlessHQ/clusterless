/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc.common;

import clusterless.cls.naming.Label;
import clusterless.cls.substrate.aws.construct.LambdaLogGroupConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.managed.ManagedConstruct;
import clusterless.cls.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.cls.substrate.aws.props.Lookup;
import clusterless.cls.substrate.aws.resources.Assets;
import clusterless.cls.substrate.aws.resources.Functions;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.lambda.Function;

import java.util.Map;
import java.util.regex.Pattern;

public class WorkloadManagedConstruct extends ManagedConstruct {
    private final Function function;

    public WorkloadManagedConstruct(@NotNull ManagedComponentContext context, Label baseId, Label modelName, String handler, LambdaJavaRuntimeProps lambdaJavaRuntimeProps, Map<String, String> environment) {
        super(context, baseId);

        String functionName = Functions.functionName(this, modelName, baseId);
        Label functionLabel = modelName.with(baseId);

        function = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionName)
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-workload.*\\.zip$"))) // get packaged code
                .architecture(Lookup.architecture(lambdaJavaRuntimeProps.architecture()))
                .handler(handler) // get handler class name
                .environment(environment)
                .runtime(Functions.defaultJVM())
                .memorySize(lambdaJavaRuntimeProps.memorySizeMB())
                .timeout(Duration.minutes(lambdaJavaRuntimeProps.timeoutMin()))
                .build();

        new LambdaLogGroupConstruct(this, functionLabel, function);
    }

    public Function function() {
        return function;
    }
}
