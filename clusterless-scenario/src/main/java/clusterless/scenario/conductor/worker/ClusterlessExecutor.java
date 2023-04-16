package clusterless.scenario.conductor.worker;

import clusterless.startup.Startup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ClusterlessExecutor {
    private static final Logger LOG = LogManager.getLogger(ClusterlessExecutor.class);

    private static final Map<String, String> properties = new LinkedHashMap<>();

    static {
        properties.put("common.resource.removeAllOnDestroy", "true");
        properties.put("aws.cdk.requireDeployApproval", "never");
        properties.put("aws.cdk.requireDestroyApproval", "false");
    }

    private final String clsApp;
    private final boolean dryRun;
    private final String command;
    private final String workingDirectory;
    private final String projectFiles;

    protected ClusterlessExecutor(String clsApp, boolean dryRun, String command, String workingDirectory, String projectFiles) {
        this.clsApp = clsApp;
        this.dryRun = dryRun;
        this.command = command;
        this.workingDirectory = workingDirectory;
        this.projectFiles = projectFiles;
    }

    public static Builder builder() {
        return Builder.aClusterlessExecutor();
    }

    private Map<String, String> getEnvironment() {
        return Collections.emptyMap();
    }

    public int exec() {
        //cls deploy -p test-s3-copy-arc-project-chain.json
        List<String> args = new LinkedList<>();

        args.add(clsApp);

        Startup.asPropertyArgs(args, properties);

        args.addAll(List.of(
                command,
                "-p",
                projectFiles,
                "-v"
        ));

        if (dryRun) {
            args.add("--dry-run");
        }

        LOG.info("working directory: {}", workingDirectory);

        Map<String, String> environment = getEnvironment();

        if (!environment.isEmpty()) {
            LOG.info("environment: {}", environment);
        }

        LOG.info("command: {}", args);

        try {
            Path cwd = Paths.get(workingDirectory);
            Path outPath = cwd.resolve("out.log");
            Path errorPath = cwd.resolve("err.log");

            ProcessBuilder processBuilder = new ProcessBuilder()
                    .command(args)
                    .directory(cwd.toFile())
                    .redirectOutput(ProcessBuilder.Redirect.appendTo(outPath.toFile()))
                    .redirectError(ProcessBuilder.Redirect.appendTo(errorPath.toFile()));

            processBuilder.environment().putAll(environment);

            Process process = processBuilder.start();

            return process.waitFor();
        } catch (IOException e) {
            LOG.error("unable to start command: " + args, e);
            return -1;
        } catch (InterruptedException e) {
            LOG.error("unable to wait for command: " + args, e);
            return -1;
        }
    }

    public static final class Builder {
        private String clsApp;
        private boolean dryRun;
        private String command;
        private String workingDirectory;
        private String projectFiles;

        private Builder() {
        }

        public static Builder aClusterlessExecutor() {
            return new Builder();
        }

        public Builder withClsApp(String clsApp) {
            this.clsApp = clsApp;
            return this;
        }

        public Builder withDryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public Builder withCommand(String command) {
            this.command = command;
            return this;
        }

        public Builder withWorkingDirectory(String workingDirectory) {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public Builder withProjectFiles(String projectFiles) {
            this.projectFiles = projectFiles;
            return this;
        }

        public ClusterlessExecutor build() {
            return new ClusterlessExecutor(clsApp, dryRun, command, workingDirectory, projectFiles);
        }
    }
}
