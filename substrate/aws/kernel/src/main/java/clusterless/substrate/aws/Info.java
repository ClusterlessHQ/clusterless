package clusterless.substrate.aws;

import picocli.CommandLine;

import java.util.List;

@CommandLine.Command(
        name = "info"
)
public class Info extends Manage {

    @CommandLine.Command(name = "version")
    public Integer version() {
        return executeCDK("--version");
    }

    @CommandLine.Command(name = "which")
    public Integer which() {
        return executeProcess("which", kernel.cdk);
    }
}
