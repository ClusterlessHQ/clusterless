package clusterless.lambda;

import clusterless.substrate.aws.sdk.EventBus;
import clusterless.substrate.aws.sdk.S3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BootstrapMachine {
    private static final Logger LOG = LogManager.getLogger(BootstrapMachine.class);
    S3 s3 = new S3();
    EventBus eventBus = new EventBus();

    public BootstrapMachine() {
    }

    public BootstrapMachine applyBucket(String bucketName) {
        if (bucketName == null) {
            return this;
        }

        LOG.info("creating bucket: {}", bucketName);

        S3.Response s3Response = s3.create(bucketName);
        if (!s3Response.isSuccess()) {
            throw new RuntimeException(s3Response.exception());
        }

        LOG.info("created bucket: {}", bucketName);

        return this;
    }

    public BootstrapMachine applyEventbus(String eventBusName) {
        if (eventBusName == null) {
            return this;
        }

        LOG.info("creating eventbus: {}", eventBusName);

        EventBus.Response eventResponse = eventBus.create(eventBusName);
        if (!eventResponse.isSuccess()) {
            throw new RuntimeException(eventResponse.exception());
        }

        LOG.info("created eventbus: {}", eventBusName);

        return this;
    }
}
