package clusterless.substrate.aws.arc.batch;

import clusterless.managed.component.ArcLocalExecutor;
import clusterless.model.deploy.Placement;
import clusterless.substrate.aws.arc.props.ArcEnvBuilder;
import clusterless.substrate.aws.event.ArcExecContext;

import java.util.List;
import java.util.Map;

public class BatchExecArcLocalExecutor implements ArcLocalExecutor {

    private final Placement placement;
    private final BatchExecArc arc;

    public BatchExecArcLocalExecutor(Placement placement, BatchExecArc arc) {
        this.placement = placement;
        this.arc = arc;
    }

    @Override
    public List<Command> commands(OS os) {
        ArcEnvBuilder arcEnvBuilder = new ArcEnvBuilder(placement, arc);
        Map<String, String> environment = arcEnvBuilder.asEnvironment();
        ArcExecContext arcExecContext = arcEnvBuilder.execContext("", "");


//        Command command = new Command();

        return null;
    }
}
