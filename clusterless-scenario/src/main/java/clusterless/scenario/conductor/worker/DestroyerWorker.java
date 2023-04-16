package clusterless.scenario.conductor.worker;

import clusterless.scenario.Options;

public class DestroyerWorker extends ClusterlessWorker {

    public DestroyerWorker(Options options) {
        super("destroy", options);
    }

    @Override
    public String getTaskDefName() {
        return "clsProjectDestroyer";
    }
}
