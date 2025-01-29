package clusterless.cls.substrate.aws.common.batch;

import java.util.Map;

public interface BatchLocalExecutor {
    default void addProvided(Map<String, String> localComments, Map<String, String> localEnvironment) {
        localComments.put("AWS_BATCH_JOB_ID", "AWS_BATCH_JOB_ID and AWS_BATCH_JOB_ATTEMPT are provided by AWS Batch");
        localEnvironment.put("AWS_BATCH_JOB_ID", "00000000-0000-0000-0000-000000000000");
        localEnvironment.put("AWS_BATCH_JOB_ATTEMPT", "1");
    }

    default void addHelper(Map<String, String> localComments, Map<String, String> localEnvironment) {
        localComments.put("CLS_LOCAL", "provided to flag execution within the local environment");
        localEnvironment.put("CLS_LOCAL", "true");
    }
}
