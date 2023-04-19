package clusterless.scenario.conductor.worker.aws;

import clusterless.json.JSONUtil;
import clusterless.scenario.Options;
import clusterless.scenario.conductor.task.aws.S3Watcher;
import clusterless.scenario.model.WatchedStore;
import clusterless.substrate.aws.sdk.S3;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class S3WatcherWorker implements Worker {
    private static final Logger LOG = LogManager.getLogger(S3WatcherWorker.class);

    protected final Options options;

    public S3WatcherWorker(Options options) {
        this.options = options;
    }

    @Override
    public String getTaskDefName() {
        return S3Watcher.AWS_S_3_WATCHER;
    }

    @Override
    public TaskResult execute(Task task) {
        Map<String, Object> inputData = task.getInputData();

        LOG.info("watcher worker starting");

        task.setStatus(Task.Status.IN_PROGRESS);

        WatchedStore store = JSONUtil.readObjectSafe((String) inputData.get("input"), WatchedStore.class);

        LOG.info("input: {}", JSONUtil.writeAsStringSafe(store));

        int exitCode = 0;

        URI uri = store.path();

        if (uri == null) {
            throw new IllegalStateException("ingressPath may not be null");
        }

        LOG.info("watching path: {}", uri);

        if (options.dryRun()) {
            LOG.info("watcher disabled, dry run");
        } else {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<Integer> result = executorService.submit(() -> pollS3Bucket(store, uri));

            try {
                exitCode = result.get(store.timeoutSec(), TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException e) {
                LOG.error("watcher timed out with: {}", e.getMessage());
                exitCode = -1;
            } catch (ExecutionException e) {
                LOG.error("watcher failed with: {}", e.getMessage(), e);
                exitCode = -1;
            }
        }

        if (exitCode == 0) {
            task.setOutputData(Map.of("complete", store.path().toString()));

            return TaskResult.complete();
        }

        return TaskResult.failed("exit code: " + exitCode);
    }

    private int pollS3Bucket(WatchedStore store, URI uri) throws InterruptedException {
        S3 s3 = new S3(null, store.region());

        while (true) {
            S3.Response response = s3.listObjects(uri);

            if (!response.isSuccess()) {
                LOG.error("unable to list path: {}, error: {}", uri, response.errorMessage());
                return -1;
            }

            List<String> objects = s3.listChildren(response);

            LOG.info("found object in path: {}, count: {}", uri, objects.size());

            if (objects.size() >= store.objectCount()) {
                break;
            }

            Thread.sleep(store.pollingSleepSec() * 1000L);
        }

        return 0;
    }
}
