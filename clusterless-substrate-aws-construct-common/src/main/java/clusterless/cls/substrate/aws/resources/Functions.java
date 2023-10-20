/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import clusterless.commons.naming.Label;
import clusterless.commons.naming.Region;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedApp;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.Objects;

public class Functions {
    public static Runtime defaultJVM() {
        return Runtime.JAVA_17;
    }

    public static String functionName(Construct scope, String modelName, String functionName) {
        Objects.requireNonNull(modelName, "modelName may not be null");
        Objects.requireNonNull(functionName, "functionName may not be null");

        return functionName(scope, modelName, Label.of(functionName));
    }

    public static String functionName(Construct scope, String modelName, Label functionName) {
        Objects.requireNonNull(modelName, "modelName may not be null");
        Objects.requireNonNull(functionName, "functionName may not be null");
        return functionName(scope, Label.of(modelName), functionName);
    }

    public static String functionName(Construct scope, Label modelName, Label functionName) {
        Objects.requireNonNull(modelName, "modelName may not be null");
        Objects.requireNonNull(functionName, "functionName may not be null");

        Label region = Region.of(Stack.of(scope).getRegion());
        Label stage = ScopedApp.scopedOf(scope).stage();
        Label project = ScopedApp.scopedOf(scope).name();
        Label version = ScopedApp.scopedOf(scope).version();

        String function = stage.upperOnly()
                .with(project)
                .with(modelName)
                .with(functionName)
                .with(version)
                .with(region)
                .lowerHyphen();

        if (function.length() > 64) {
            throw new IllegalStateException("function name too long, must be < 64 characters, got: " + function);
        }

        return function;
    }
}
