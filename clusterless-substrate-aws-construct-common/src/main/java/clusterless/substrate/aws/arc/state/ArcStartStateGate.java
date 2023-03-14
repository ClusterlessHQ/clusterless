package clusterless.substrate.aws.arc.state;

import clusterless.lambda.arc.ArcStateProps;
import clusterless.lambda.arc.ArcStateStartHandler;
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

public class ArcStartStateGate extends ArcStateMachineFragment {
    public ArcStartStateGate(@NotNull ManagedComponentContext context, ArcStateProps arcStateProps, LambdaJavaRuntimeProps runtimeProps) {
        super(context, Label.of("ArcStartStateGate"), arcStateProps);

        Map<String, String> environment = Env.toEnv(arcStateProps);

        Label functionLabel = Label.of("ArcStartGate");

        // get packaged code
        // get handler class name
        Function function = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionLabel.lowerHyphen())
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-arc.*\\.zip$"))) // get packaged code
                .handler(ArcStateStartHandler.class.getName()) // get handler class name
                .environment(environment)
                .runtime(Runtime.JAVA_11)
                .architecture(Lookup.architecture(runtimeProps.architecture()))
                .memorySize(runtimeProps.memorySizeMB())
                .timeout(Duration.minutes(runtimeProps.timeoutMin()))
                .build();

        grantPermissionsTo(function);

        LambdaInvoke startState = LambdaInvoke.Builder.create(this, "ArcStartGateInvoke")
                .lambdaFunction(function)
                .payloadResponseOnly(true) // sets .invocationType(LambdaInvocationType.REQUEST_RESPONSE)
                .retryOnServiceExceptions(true)
                .build();

        setStartState(startState);
        setEndStates(startState.getEndStates());
    }
}
