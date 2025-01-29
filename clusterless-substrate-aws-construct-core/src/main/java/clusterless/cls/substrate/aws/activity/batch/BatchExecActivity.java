package clusterless.cls.substrate.aws.activity.batch;

import clusterless.cls.json.JsonRequiredProperty;
import clusterless.cls.model.deploy.Activity;
import clusterless.cls.substrate.aws.props.BatchRuntimeProps;

import java.net.URI;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class BatchExecActivity extends Activity {
    private BatchRuntimeProps batchRuntimeProps = new BatchRuntimeProps();
    @JsonRequiredProperty
    private String computeEnvironmentRef;
    @JsonRequiredProperty
    private Path imagePath;
    private Map<String, String> environment = new LinkedHashMap<>();
    private List<String> command = new LinkedList<>();
    private String bucketRef;
    @JsonRequiredProperty
    private URI pathURI;

    public BatchRuntimeProps batchRuntimeProps() {
        return batchRuntimeProps;
    }

    public String computeEnvironmentRef() {
        return computeEnvironmentRef;
    }

    public Path imagePath() {
        return imagePath;
    }

    public Map<String, String> environment() {
        return environment;
    }

    public List<String> command() {
        return command;
    }

    public String bucketRef() {
        return bucketRef;
    }

    public URI pathURI() {
        return pathURI;
    }
}
