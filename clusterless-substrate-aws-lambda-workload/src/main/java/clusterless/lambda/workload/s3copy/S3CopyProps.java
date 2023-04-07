package clusterless.lambda.workload.s3copy;

import clusterless.model.deploy.WorkloadProps;

public class S3CopyProps extends WorkloadProps {
    float failArcOnPartialPercent = 1.0f;

    public float failArcOnPartialPercent() {
        return failArcOnPartialPercent;
    }
}
