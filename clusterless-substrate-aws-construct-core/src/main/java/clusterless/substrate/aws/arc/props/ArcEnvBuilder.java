package clusterless.substrate.aws.arc.props;

import clusterless.lambda.arc.ArcProps;
import clusterless.model.deploy.*;
import clusterless.model.manifest.ManifestState;
import clusterless.substrate.aws.event.ArcExecContext;
import clusterless.substrate.aws.event.ArcNotifyEvent;
import clusterless.substrate.aws.resources.StateURIs;
import clusterless.substrate.aws.uri.ManifestURI;
import clusterless.util.Env;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

public class ArcEnvBuilder {
    private final Placement placement;
    private final Arc<? extends Workload<? extends WorkloadProps>> model;
    private final ArcProps<WorkloadProps> arcProps;

    public ArcEnvBuilder(Placement placement, Arc<? extends Workload<? extends WorkloadProps>> model) {
        this.placement = placement;
        this.model = model;
        this.arcProps = createArcProps();
    }

    protected ArcProps<WorkloadProps> createArcProps() {
        Map<String, ManifestURI> sourceManifestPaths = model.sources()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> StateURIs.manifestPath(placement, ManifestState.complete, e.getValue())));

        Map<String, ManifestURI> sinkManifestPaths = model.sinks()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> StateURIs.manifestPath(placement, e.getValue())));

        return ArcProps.builder()
                .withSources(model.sources())
                .withSinks(model.sinks())
                .withSourceManifestPaths(sourceManifestPaths)
                .withSinkManifestPaths(sinkManifestPaths)
                .withWorkloadProps(model.workload().workloadProps())
                .build();
    }

    public ArcProps<WorkloadProps> arcProps() {
        return arcProps;
    }

    public Map<String, String> asEnvironment() {
        return Env.toEnv(arcProps());
    }

    @NotNull
    public ArcExecContext execContext(String role, String lotId) {
        URI manifest = arcProps.sourceManifestPaths()
                .get(role)
                .withState(ManifestState.complete)
                .withLot(lotId)
                .uri();

        SourceDataset dataset = model.sources().get(role);

        return ArcExecContext.builder()
                .withArcNotifyEvent(
                        ArcNotifyEvent.Builder.builder()
                                .withDataset(dataset)
                                .withManifest(manifest)
                                .withLotId(lotId)
                                .build()
                )
                .withRole(role)
                .build();
    }
}
