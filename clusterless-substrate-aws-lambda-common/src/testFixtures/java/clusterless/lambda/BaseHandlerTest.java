package clusterless.lambda;

import clusterless.model.Struct;
import clusterless.substrate.aws.sdk.EventBus;
import clusterless.substrate.aws.sdk.S3;
import clusterless.util.Env;
import com.amazonaws.services.lambda.runtime.Context;
import org.jetbrains.annotations.NotNull;
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
public abstract class BaseHandlerTest {
    static DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:1.4.0");

    @Container
    static LocalStackContainer localstack = new LocalStackContainer(localstackImage)
            .withServices(
                    LocalStackContainer.Service.S3,
                    LocalStackContainer.EnabledService.named("events")
            );

    @SystemStub
    private EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(Env.key(getProps()), Env.value(getProps()))
            .set("AWS_ACCESS_KEY_ID", localstack.getAccessKey())
            .set("AWS_SECRET_ACCESS_KEY", localstack.getSecretKey())
            .set("AWS_DEFAULT_REGION", localstack.getRegion())
            .set("AWS_S3_ENDPOINT", localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());

    protected abstract Struct getProps();

    @NotNull
    protected String eventBusName() {
        return "forwarding-bus";
    }

    protected Context context() {
        return null;
    }

    @BeforeEach
    void setUp() {
        S3 s3 = new S3();
        S3.Response s3Response = s3.create(TestDatasets.MANIFEST_BUCKET);
        if (!s3Response.isSuccess()) {
            throw new RuntimeException(s3Response.exception());
        }

        String eventBusName = eventBusName();
        if (eventBusName != null) {
            EventBus eventBus = new EventBus();
            EventBus.Response eventResponse = eventBus.create(eventBusName);
            if (!eventResponse.isSuccess()) {
                throw new RuntimeException(eventResponse.exception());
            }
        }
    }
}
