package clusterless.lambda;

import clusterless.substrate.aws.store.StateStore;
import clusterless.substrate.aws.store.Stores;
import clusterless.util.Env;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@Testcontainers
@ExtendWith(SystemStubsExtension.class)
public abstract class LocalStackBase extends LambdaHandlerTestBase {
    static {
        System.setProperty("clusterless.localstack.enabled", "true");
    }

    static DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:1.4.0");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(localstackImage)
            .withServices(
                    LocalStackContainer.Service.S3,
                    LocalStackContainer.EnabledService.named("events")
            );

    @Override
    protected String defaultRegion() {
        return localstack.getRegion();
    }

    @SystemStub
    private EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(Env.keyTyped(getProps()), Env.valueTyped(getProps())) // shove the props json into an env var
            .set("AWS_ACCESS_KEY_ID", localstack.getAccessKey())
            .set("AWS_SECRET_ACCESS_KEY", localstack.getSecretKey())
            .set("AWS_DEFAULT_REGION", localstack.getRegion())
            .set("AWS_S3_ENDPOINT", localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());

    @BeforeEach
    public void bootstrap() {
        new BootstrapMachine()
                .applyBucket(Stores.bootstrapStoreName(StateStore.Manifest, defaultPlacement()))
                .applyBucket(Stores.bootstrapStoreName(StateStore.Arc, defaultPlacement()))
                .applyBucket(Stores.bootstrapStoreName(StateStore.Meta, defaultPlacement()))
                .applyEventbus(eventBusName());
    }
}
