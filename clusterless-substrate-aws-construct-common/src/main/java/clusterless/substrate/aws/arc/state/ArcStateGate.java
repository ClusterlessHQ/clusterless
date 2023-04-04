package clusterless.substrate.aws.arc.state;

import clusterless.lambda.arc.ArcStateProps;
import clusterless.model.deploy.Arc;
import clusterless.substrate.aws.arc.ArcStateMachineFragment;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.substrate.aws.props.Lookup;
import clusterless.substrate.aws.resources.Assets;
import clusterless.util.Env;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;

import java.util.Map;
import java.util.regex.Pattern;

public abstract class ArcStateGate extends ArcStateMachineFragment {
    public ArcStateGate(@NotNull ManagedComponentContext context, Label baseId, Arc<?> arc, ArcStateProps arcStateProps, String handlerClassName, LambdaJavaRuntimeProps runtimeProps) {
        super(context, baseId, arcStateProps);

        Map<String, String> environment = Env.toEnv(arcStateProps);

        Label project = context.managedProject().name();
        String version = context.managedProject().version();
        String arcName = arc.name();

        Label functionName = project.with(arcName).with(baseId).with(version);

        // get packaged code
        // get handler class name
        Function function = Function.Builder.create(this, baseId.with("Function").camelCase())
                .functionName(functionName.lowerHyphen())
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-arc.*\\.zip$"))) // get packaged code
                .handler(handlerClassName) // get handler class name
                .environment(environment)
                .runtime(Runtime.JAVA_11)
                .architecture(Lookup.architecture(runtimeProps.architecture()))
                .memorySize(runtimeProps.memorySizeMB())
                .timeout(Duration.minutes(runtimeProps.timeoutMin()))
                .build();

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