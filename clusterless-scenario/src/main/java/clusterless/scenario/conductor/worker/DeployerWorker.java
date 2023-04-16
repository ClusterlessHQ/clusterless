package clusterless.scenario.conductor.worker;

import clusterless.scenario.Options;

public class DeployerWorker extends ClusterlessWorker {

    public DeployerWorker(Options options) {
        super("deploy", options);
    }

    @Override
    public String getTaskDefName() {
        return "clsProjectDeployer";
    }
}
