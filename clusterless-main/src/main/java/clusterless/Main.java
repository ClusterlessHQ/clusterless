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
import clusterless.model.deploy.Deployable;
import clusterless.startup.Loader;
import clusterless.startup.Startup;
import clusterless.substrate.ProviderSubstratesOptions;
import clusterless.substrate.SubstrateProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
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
                ShowCommand.class
        }
)
public class Main extends Startup implements Callable<Integer> {
    private static final Logger LOG = LogManager.getLogger(Main.class);
    @CommandLine.Mixin
    protected ProviderSubstratesOptions providerSubstratesOptions = new ProviderSubstratesOptions();
    private String[] args;

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new Main(args));

        commandLine.addSubcommand("bootstrap", new CommandWrapper(new BootstrapCommandOptions()));
        commandLine.addSubcommand("verify", new CommandWrapper(new VerifyCommandOptions()));
        commandLine.addSubcommand("deploy", new CommandWrapper(new DeployCommandOptions()));
        commandLine.addSubcommand("destroy", new CommandWrapper(new DestroyCommandOptions()));
        commandLine.addSubcommand("diff", new CommandWrapper(new DiffCommandOptions()));

        commandLine.parseArgs(args);

        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }

        commandLine.execute(args);
    }

    public Main(String[] args) {
        this.args = args;
    }

    public ProviderSubstratesOptions substratesOptions() {
        return providerSubstratesOptions;
    }

    public CommandLine.IExitCodeExceptionMapper getExitCodeExceptionMapper() {
        return new ExitCodeExceptionMapper();
    }

    @Override
    public Integer call() {
        return 0;
    }

    public Integer run(CommonCommandOptions command) throws IOException {
        if (command instanceof LifecycleCommandOptions) {
            return run((LifecycleCommandOptions) command);
        }

        return run(substratesOptions().substrates());
    }

    public Integer run(LifecycleCommandOptions command) throws IOException {

        Loader loader = new Loader(command.projectFiles());

        List<String> declaredProviders = loader.getStringsAt(Deployable.PROVIDER_POINTER);

        LOG.info("files: {}", command.projectFiles());
        LOG.info("declared: {}", declaredProviders);

        return run(declaredProviders);
    }

    public int run(Collection<String> declaredProviders) {
        Map<String, SubstrateProvider> substrates = substratesOptions().requestedSubstrates();

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
}
