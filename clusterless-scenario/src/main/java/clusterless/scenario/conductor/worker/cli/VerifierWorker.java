package clusterless.scenario.conductor.worker.cli;

import clusterless.scenario.Options;
import clusterless.scenario.conductor.task.cli.Verifier;

public class VerifierWorker extends ClusterlessWorker {

    public VerifierWorker(Options options) {
        super("verify", options);
    }

    @Override
    public String getTaskDefName() {
        return Verifier.CLS_PROJECT_VERIFIER;
    }

    @Override
    protected boolean getDryRun() {
        return (!super.getDryRun() || !options.verifyOnDryRun()) && super.getDryRun();
    }
}
