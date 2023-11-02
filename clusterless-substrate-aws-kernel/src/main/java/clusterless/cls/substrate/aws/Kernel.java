/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws;

import clusterless.cls.config.ConfigManager;
import clusterless.cls.config.Configuration;
import clusterless.cls.managed.component.Component;
import clusterless.cls.managed.component.ComponentContext;
import clusterless.cls.managed.component.ComponentService;
import clusterless.cls.managed.component.ComponentServices;
import clusterless.cls.model.Model;
import clusterless.cls.model.Struct;
import clusterless.cls.startup.Startup;
import clusterless.cls.substrate.ProviderSubstratesOptions;
import clusterless.cls.substrate.SubstrateProvider;
import clusterless.cls.substrate.aws.cdk.bootstrap.Bootstrap;
import clusterless.cls.substrate.aws.cdk.lifecycle.*;
import clusterless.cls.substrate.aws.local.Local;
import clusterless.cls.substrate.aws.report.Arcs;
import clusterless.cls.substrate.aws.report.Placements;
import clusterless.cls.substrate.aws.report.Projects;
import clusterless.cls.util.ExecutionExceptionHandler;
import clusterless.cls.util.ExitCodeExceptionMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@CommandLine.Command(
        mixinStandardHelpOptions = true,
        scope = CommandLine.ScopeType.INHERIT,
        subcommands = {
                Bootstrap.class,
                Info.class,
                Verify.class,
                Diff.class,
                Deploy.class,
                Import.class,
                Destroy.class,
                Synth.class,
                Local.class,
                Placements.class,
                Projects.class,
                Arcs.class
        }
)
public class Kernel extends Startup implements SubstrateProvider {
    private static final Logger LOG = LogManager.getLogger(Kernel.class);

    public static void main(String[] args) {
        System.exit(new Kernel().execute(args));
    }

    /**
     * added so --providers is parsed, but ignored. need a better solution
     */
    @CommandLine.Mixin
    ProviderSubstratesOptions providerSubstratesOptions = ProviderSubstratesOptions.ignored();

    public Kernel() {
        configurations().add(ConfigManager.optionsFor(providerName(), AwsConfig.class));
    }

    @Override
    public String providerName() {
        return "aws";
    }

    @Override
    public Map<String, Class<? extends Struct>> models() {
        return ComponentServices.INSTANCE.componentServices().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().modelClass()));
    }

    @Override
    public Map<String, ComponentService<ComponentContext, Model, Component>> components() {
        return ComponentServices.INSTANCE.componentServices().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Class<? extends Configuration> configClass() {
        return AwsConfig.class;
    }

    @Override
    public int execute(String[] args) {
        LOG.info("kernel: {} ", Arrays.toString(args));

        return new CommandLine(this)
                .setExitCodeExceptionMapper(new ExitCodeExceptionMapper())
                .setExecutionExceptionHandler(new ExecutionExceptionHandler(this))
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }
}
