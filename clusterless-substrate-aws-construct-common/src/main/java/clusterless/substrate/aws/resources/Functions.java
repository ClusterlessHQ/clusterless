package clusterless.substrate.aws.resources;

import clusterless.model.deploy.Region;
import clusterless.substrate.aws.managed.ManagedProject;
import clusterless.substrate.aws.managed.StagedApp;
import clusterless.util.Label;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

import java.util.Objects;

public class Functions {
    public static String functionName(Construct scope, String modelName, String functionName) {
        Objects.requireNonNull(modelName, "modelName may not be null");
        Objects.requireNonNull(functionName, "functionName may not be null");

        return functionName(scope, modelName, Label.of(functionName));
    }

    public static String functionName(Construct scope, String modelName, Label functionName) {
        Objects.requireNonNull(modelName, "modelName may not be null");
        Objects.requireNonNull(functionName, "functionName may not be null");

        Region region = Region.of(Stack.of(scope).getRegion());
        Label stage = StagedApp.stagedOf(scope).stage();
        Label project = ManagedProject.projectOf(scope).name();
        String version = ManagedProject.projectOf(scope).version();

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
