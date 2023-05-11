package clusterless.substrate.aws.local;

import clusterless.command.LocalCommandOptions;
import clusterless.substrate.aws.ProcessExec;

import java.util.Map;

public class LocalProcessExec extends ProcessExec {
    public LocalProcessExec(LocalCommandOptions commandOptions) {
        super(commandOptions::dryRun);
    }

    @Override
    protected Map<String, String> getCommonEnvironment() {
        return null;
    }
}
