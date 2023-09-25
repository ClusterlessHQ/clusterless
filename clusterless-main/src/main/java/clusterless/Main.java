/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

/*
 * This Java source file was generated by the Gradle 'init' task.
 */

package clusterless;

import clusterless.command.*;
import clusterless.model.Loader;
import clusterless.model.deploy.Deployable;
import clusterless.startup.Startup;
import clusterless.substrate.ProviderSubstratesOptions;
import clusterless.substrate.SubstrateProvider;
import clusterless.util.ExecutionExceptionHandler;
import clusterless.util.ExitCodeExceptionMapper;
import clusterless.util.ParameterExceptionHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "cls",
        mixinStandardHelpOptions = true,
        version = "1.0-wip",
        subcommands = {
                CommandLine.HelpCommand.class,
                ConfigCommand.class,
                ShowCommand.class,
                BootstrapCommand.class,
                DeployCommand.class,
                DestroyCommand.class,
                DiffCommand.class,
                ImportCommand.class,
                LocalCommand.class,
                VerifyCommand.class,
                PlacementsCommand.class,
                ProjectsCommand.class
        }
)
public class Main extends Startup implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Main.class);

    /**
     * This provides a global --providers predicate, but when calling a provider, we need to sort out a way
     * to remove these args. currently the provider class parses the options, but ignores them
     */
    @CommandLine.Mixin
    protected ProviderSubstratesOptions providerSubstratesOptions = new ProviderSubstratesOptions();
    private String[] args;

    public static void main(String[] args) {
        Main main = new Main(args);

        CommandLine commandLine = new CommandLine(main);

        commandLine
                .setExitCodeExceptionMapper(new ExitCodeExceptionMapper())
                .setExecutionExceptionHandler(new ExecutionExceptionHandler(main))
                .setParameterExceptionHandler(new ParameterExceptionHandler(main));

        System.exit(commandLine.execute(args));
    }

    public Main(String[] args) {
        this.args = args;
    }

    // allows doc generation
    public Main() {
    }

    public ProviderSubstratesOptions substratesOptions() {
        return providerSubstratesOptions;
    }

    @Override
    public Integer call() {
        return 0;
    }

    public Integer run(CommonCommandOptions command) throws IOException {
        if (command instanceof ProjectCommandOptions) {
            return run((ProjectCommandOptions) command);
        }

        return run(substratesOptions().providerNames(), args);
    }

    public Integer run(ProjectCommandOptions command) throws IOException {

        Loader loader = new Loader(command.projectFiles());

        List<String> declaredProviders = loader.getStringsAt(Deployable.PROVIDER_POINTER);

        LOG.info("files: {}", command.projectFiles());
        LOG.info("declared: {}", declaredProviders);

        String[] argsArray = replaceWithTemp(command);

        return run(declaredProviders, argsArray);
    }

    public int run(Collection<String> declaredProviders, String[] args) {
        Map<String, SubstrateProvider> substrates = substratesOptions().requestedProvider();

        LOG.info("available: {}", substrates.keySet());

        int result = 0;
        for (String declaredProvider : declaredProviders) {
            SubstrateProvider substrateProvider = substrates.get(declaredProvider);

            if (substrateProvider == null) {
                throw new IllegalStateException("substrate not found: " + declaredProvider);
            }

            int execute = substrateProvider.execute(args);

            if (execute != 0) {
                return execute;
            }
        }

        return result;
    }

    @NotNull
    private String[] replaceWithTemp(ProjectCommandOptions command) {
        List<String> args = Arrays.asList(this.args);

        int i = args.indexOf("-p");

        if (i == -1) {
            i = args.indexOf("--project");
        }

        if (i != -1 && "-".equals(args.get(i + 1))) {
            args.set(i + 1, command.projectFiles().get(0).toString());
        }

        return args.toArray(new String[0]);
    }
}
