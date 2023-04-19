package clusterless.scenario.conductor.worker.cli;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.task.cli.Deployer;

public class DeployerWorker extends ClusterlessWorker {

    public DeployerWorker(Options options) {
        super("deploy", options);
    }

    @Override
    public String getTaskDefName() {
        return Deployer.CLS_PROJECT_DEPLOYER;
    }
}
