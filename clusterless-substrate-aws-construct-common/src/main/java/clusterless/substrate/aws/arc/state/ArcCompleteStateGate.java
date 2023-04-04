package clusterless.substrate.aws.arc.state;

import clusterless.lambda.arc.ArcStateCompleteHandler;
import clusterless.lambda.arc.ArcStateProps;
import clusterless.model.deploy.Arc;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.props.LambdaJavaRuntimeProps;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.iam.IGrantable;

public class ArcCompleteStateGate extends ArcStateGate {
    public ArcCompleteStateGate(@NotNull ManagedComponentContext context, Arc<?> arc, ArcStateProps arcStateProps, LambdaJavaRuntimeProps runtimeProps) {
        super(context, Label.of("ArcCompleteStateGate"), arc, arcStateProps, ArcStateCompleteHandler.class.getName(), runtimeProps);
    }

    @Override
    protected void grantPermissionsTo(IGrantable grantable) {
        grantBootstrapReadWrite(grantable);
        grantEventBus(grantable);
    }
}
