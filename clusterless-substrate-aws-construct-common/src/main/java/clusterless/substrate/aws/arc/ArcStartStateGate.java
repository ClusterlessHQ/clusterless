package clusterless.substrate.aws.arc;

import clusterless.lambda.arc.ArcStateProps;
import clusterless.lambda.arc.ArcStateStartHandler;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedConstruct;
import clusterless.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.substrate.aws.props.Lookup;
import clusterless.substrate.aws.resources.Assets;
import clusterless.util.Env;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.stepfunctions.State;
import software.amazon.awscdk.services.stepfunctions.tasks.LambdaInvoke;

import java.util.Map;
import java.util.regex.Pattern;

public class ArcStartStateGate extends ManagedConstruct {

    private final Function function;

    public ArcStartStateGate(@NotNull ManagedComponentContext context, ArcStateProps arcStateProps, LambdaJavaRuntimeProps runtimeProps) {
        super(context, Label.of("ArcStartStateGate"));

        Map<String, String> environment = Env.toEnv(arcStateProps);

        Label functionLabel = Label.of("ArcStartGate");

        function = Function.Builder.create(this, functionLabel.camelCase())
                .functionName(functionLabel.lowerHyphen())
                .code(Assets.find(Pattern.compile("^.*-aws-lambda-arc.*\\.zip$"))) // get packaged code
                .handler(ArcStateStartHandler.class.getName()) // get handler class name
                .environment(environment)
                .runtime(Runtime.JAVA_11)
                .architecture(Lookup.architecture(runtimeProps.architecture()))
                .memorySize(runtimeProps.memorySizeMB())
                .timeout(Duration.minutes(runtimeProps.timeoutMin()))
                .build();
    }

    public Function function() {
        return function;
    }

    public State createState() {
        return LambdaInvoke.Builder.create(this, "ArcStartGateInvoke")
                .lambdaFunction(function())
                .payloadResponseOnly(true) // sets .invocationType(LambdaInvocationType.REQUEST_RESPONSE)
                .build();
    }

}
