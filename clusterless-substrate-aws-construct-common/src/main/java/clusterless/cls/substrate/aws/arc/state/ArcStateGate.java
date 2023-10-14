/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.arc.state;

import clusterless.aws.lambda.arc.ArcStateProps;
import clusterless.cls.model.deploy.Arc;
import clusterless.cls.naming.Label;
import clusterless.cls.substrate.aws.arc.ArcStateMachineFragment;
import clusterless.cls.substrate.aws.construct.LambdaLogGroupConstruct;
import clusterless.cls.substrate.aws.managed.ManagedComponentContext;
import clusterless.cls.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.cls.substrate.aws.props.Lookup;
import clusterless.cls.substrate.aws.resources.Assets;
import clusterless.cls.substrate.aws.resources.Functions;
import clusterless.cls.util.Env;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;

import java.util.Map;
import java.util.regex.Pattern;

public abstract class ArcStateGate extends ArcStateMachineFragment {
    public ArcStateGate(@NotNull ManagedComponentContext context, Label baseId, Arc<?> arc, ArcStateProps arcStateProps, String handlerClassName, LambdaJavaRuntimeProps runtimeProps) {
        super(context, baseId, arcStateProps);

        Map<String, String> environment = Env.toEnv(arcStateProps);

        String functionName = Functions.functionName(this, arc.name(), baseId);

        // get packaged code
        // get handler class name
        Label functionLabel = baseId.with("Function");
        Function function = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionName)
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-arc.*\\.zip$"))) // get packaged code
                .handler(handlerClassName) // get handler class name
                .environment(environment)
                .runtime(Functions.defaultJVM())
                .architecture(Lookup.architecture(runtimeProps.architecture()))
                .memorySize(runtimeProps.memorySizeMB())
                .timeout(Duration.minutes(runtimeProps.timeoutMin()))
                .build();

        new LambdaLogGroupConstruct(this, functionLabel, function);

        grantPermissionsTo(function);

        LambdaInvoke startState = LambdaInvoke.Builder.create(this, baseId.with("Invoke").camelCase())
                .lambdaFunction(function)
                .payloadResponseOnly(true) // sets .invocationType(LambdaInvocationType.REQUEST_RESPONSE)
                .retryOnServiceExceptions(true)
                .build();

        setStartState(startState);
        setEndStates(startState.getEndStates());
    }
}
