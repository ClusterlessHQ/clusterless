package clusterless.substrate.aws.arc;

import clusterless.lambda.arc.ArcStateProps;
import clusterless.model.deploy.Dataset;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedStateMachineFragment;
import clusterless.substrate.aws.resources.BootstrapStores;
import clusterless.substrate.aws.resources.Events;
import clusterless.util.Label;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.events.IEventBus;
import software.amazon.awscdk.services.iam.IGrantable;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.IBucket;

import java.util.Map;
import java.util.function.Consumer;

public abstract class ArcStateMachineFragment extends ManagedStateMachineFragment {
    protected final ArcStateProps arcStateProps;

    public ArcStateMachineFragment(@NotNull ManagedComponentContext context, Label baseId, ArcStateProps arcStateProps) {
        super(context, baseId);
        this.arcStateProps = arcStateProps;
    }

    public ArcStateProps arcStateProps() {
        return arcStateProps;
    }

    protected String idFor(String value) {
        return Label.of(arcStateProps().name())
                .with(Label.of(value))
                .camelCase();
    }

    protected abstract void grantPermissionsTo(IGrantable grantable);

    protected void grantEventBus(IGrantable grantable) {
        String eventBusRef = Events.arcEventBusNameRef(this);
        IEventBus arcEventBus = EventBus.fromEventBusName(this, "EventBus", eventBusRef);
        arcEventBus.grantPutEventsTo(grantable);
    }

    protected void grantBootstrapReadWrite(@NotNull IGrantable grantee) {
        BootstrapStores.arcStateBucket(this).grantReadWrite(grantee);
        BootstrapStores.manifestBucket(this).grantReadWrite(grantee);
    }

    protected void grantDatasets(IGrantable grantable) {
        grantEach(arcStateProps().sources(), idFor("Source"), b -> b.grantRead(grantable));
        grantEach(arcStateProps().sinks(), idFor("Sink"), b -> b.grantReadWrite(grantable));
    }

    protected void grantEach(Map<String, ? extends Dataset> sources, String id, Consumer<IBucket> grant) {
        sources.forEach((key, value) -> {
            String baseId = Label.of(id).with(key).camelCase();
            String bucketName = value.pathURI().getHost();
            grant.accept(Bucket.fromBucketName(this, baseId, bucketName));
        });
    }
}
