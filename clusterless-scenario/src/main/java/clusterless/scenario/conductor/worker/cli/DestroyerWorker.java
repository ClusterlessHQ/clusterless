package clusterless.scenario.conductor.worker.cli;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.task.cli.Destroyer;

public class DestroyerWorker extends ClusterlessWorker {

    public DestroyerWorker(Options options) {
        super("destroy", options);
    }

    @Override
    public String getTaskDefName() {
        return Destroyer.CLS_PROJECT_DESTROYER;
    }
}
