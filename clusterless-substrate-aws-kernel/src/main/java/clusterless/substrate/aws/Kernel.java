/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.config.ConfigManager;
import clusterless.config.Configuration;
import clusterless.managed.component.ComponentServices;
import clusterless.model.Struct;
import clusterless.startup.Startup;
import clusterless.substrate.ProviderSubstratesOptions;
import clusterless.substrate.SubstrateProvider;
import clusterless.substrate.aws.bootstrap.Bootstrap;
import clusterless.substrate.aws.cdk.*;
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
                Destroy.class,
                Synth.class,
                Report.class
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
    public Class<? extends Configuration> configClass() {
        return AwsConfig.class;
    }

    @Override
    public int execute(String[] args) {
        LOG.info("kernel: {} ", Arrays.toString(args));

        return new CommandLine(this)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }
}
