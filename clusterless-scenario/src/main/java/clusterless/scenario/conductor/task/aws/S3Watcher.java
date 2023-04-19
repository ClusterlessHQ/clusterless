package clusterless.scenario.conductor.task.aws;

import clusterless.json.JSONUtil;
import clusterless.scenario.conductor.task.BaseSimpleTask;
import clusterless.scenario.model.WatchedStore;

public class S3Watcher extends BaseSimpleTask {

    public static final String AWS_S_3_WATCHER = "awsS3Watcher";

    public S3Watcher(String taskReferenceName, WatchedStore store) {
        super(AWS_S_3_WATCHER, taskReferenceName);

        input("input", JSONUtil.writeAsStringSafe(store));
    }
}
