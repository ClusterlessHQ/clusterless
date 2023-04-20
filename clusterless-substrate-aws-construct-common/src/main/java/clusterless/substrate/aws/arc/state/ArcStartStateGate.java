package clusterless.substrate.aws.arc.state;

import clusterless.lambda.arc.ArcStateProps;
import clusterless.lambda.arc.ArcStateStartHandler;
import clusterless.model.deploy.Arc;
import clusterless.naming.Label;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.props.LambdaJavaRuntimeProps;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.iam.IGrantable;

public class ArcStartStateGate extends ArcStateGate {
    public ArcStartStateGate(@NotNull ManagedComponentContext context, Arc<?> arc, ArcStateProps arcStateProps, LambdaJavaRuntimeProps runtimeProps) {
        super(context, Label.of("ArcStart"), arc, arcStateProps, ArcStateStartHandler.class.getName(), runtimeProps);
    }

    @Override
    protected void grantPermissionsTo(IGrantable grantable) {
        grantBootstrapReadWrite(grantable);
    }
}
