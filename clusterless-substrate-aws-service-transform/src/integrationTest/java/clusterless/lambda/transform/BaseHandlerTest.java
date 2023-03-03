package clusterless.lambda.transform;

import clusterless.json.JSONUtil;
import clusterless.model.Struct;
import clusterless.model.deploy.Dataset;
import clusterless.model.deploy.SourceDataset;
import clusterless.substrate.aws.sdk.EventBus;
import clusterless.substrate.aws.sdk.S3;
import clusterless.util.Env;
import clusterless.util.URIs;
import com.adelean.inject.resources.junit.jupiter.TestWithResources;
import com.adelean.inject.resources.junit.jupiter.WithJacksonMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.net.URI;

@Testcontainers
@TestWithResources
@ExtendWith(SystemStubsExtension.class)
public abstract class BaseHandlerTest {
    static DockerImageName localstackImage = DockerImageName.parse("localstack/localstack:1.3.1");
    @Container
    static LocalStackContainer localstack = new LocalStackContainer(localstackImage)
            .withServices(
                    LocalStackContainer.Service.S3,
                    LocalStackContainer.EnabledService.named("events")
            );
    @WithJacksonMapper
    static ObjectMapper objectMapper = JSONUtil.OBJECT_MAPPER;
    @SystemStub
    private EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(Env.key(getProps()), Env.value(getProps()))
            .set("AWS_ACCESS_KEY_ID", localstack.getAccessKey())
            .set("AWS_SECRET_ACCESS_KEY", localstack.getSecretKey())
            .set("AWS_DEFAULT_REGION", localstack.getRegion())
            .set("AWS_S3_ENDPOINT", localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());

    public Dataset getDataset() {
        return SourceDataset.Builder.builder()
                .withName("dataset")
                .withVersion("20230101")
                .build();
    }

    protected URI getManifestURI() {
        return URIs.copyAppendPath(URI.create("s3://manifests/"), getDataset().name(), getDataset().version());
    }

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
        S3.Response s3Response = s3.create(getManifestURI().getHost());
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
