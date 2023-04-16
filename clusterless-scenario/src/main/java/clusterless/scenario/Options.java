package clusterless.scenario;

import clusterless.util.Runtimes;
import picocli.CommandLine;

import java.nio.file.Paths;

public class Options {
    @CommandLine.Option(names = "--cls-app", description = "path to the cls-aws kernel")
    private String clsApp = Runtimes.findExecutable("cls").orElse(Paths.get("cls")).toAbsolutePath().toString();

    @CommandLine.Option(names = "--dry-run", description = "do not execute underlying cdk binary")
    private boolean dryRun = false;

    public String clsApp() {
        return clsApp;
    }

    public boolean dryRun() {
        return dryRun;
    }
}
