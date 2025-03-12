package clusterless.cls.substrate.aws.common.batch;

import java.util.Map;

/**
 * Interface for adding AWS Batch specific local execution environment variables and comments.
 * This is used to provide information about the execution environment when running locally.
 * <p>
 * {@code runInDocker} indicates whether the execution is happening in a Docker container or not.
 */
public interface BatchLocalExecutor {
    default void addProvided(Map<String, String> localComments, Map<String, String> localEnvironment, boolean runInDocker) {
        localComments.put("AWS_BATCH_JOB_ID", "AWS_BATCH_JOB_ID and AWS_BATCH_JOB_ATTEMPT are provided by AWS Batch");
        localEnvironment.put("AWS_BATCH_JOB_ID", "00000000-0000-0000-0000-000000000000");
        localEnvironment.put("AWS_BATCH_JOB_ATTEMPT", "1");
    }

    default void addHelper(Map<String, String> localComments, Map<String, String> localEnvironment, boolean runInDocker) {
        if (runInDocker) return;

        localComments.put("CLS_LOCAL", "provided to flag execution within the local environment");
        localEnvironment.put("CLS_LOCAL", "true");
    }
}
