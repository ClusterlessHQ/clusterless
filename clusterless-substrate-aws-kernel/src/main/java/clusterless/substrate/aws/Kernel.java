/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.managed.component.ComponentServices;
import clusterless.model.Struct;
import clusterless.startup.Startup;
import clusterless.substrate.SubstrateProvider;
import clusterless.substrate.aws.bootstrap.Bootstrap;
import clusterless.substrate.aws.cdk.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {
        Bootstrap.class,
        Info.class,
        Verify.class,
        Diff.class,
        Deploy.class,
        Destroy.class,
        Synth.class,
        Report.class
})
public class Kernel extends Startup implements SubstrateProvider {
    private static final Logger LOG = LogManager.getLogger(Kernel.class);

    /**
     * put here as a placeholder, unused
     */
    @CommandLine.Option(names = {"-s", "--substrate"}, description = "substrates to target", scope = CommandLine.ScopeType.INHERIT)
    protected Set<String> substrates = new LinkedHashSet<>();

    public static void main(String[] args) {
        System.exit(new Kernel().execute(args));
    }

    @Override
    public String substrate() {
        return "aws";
    }

    @Override
    public Map<String, Class<? extends Struct>> models() {
        return ComponentServices.INSTANCE.componentServices().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().modelClass()));
    }

    @Override
    public int execute(String[] args) {
        LOG.info("kernel: {} ", Arrays.toString(args));

        return new CommandLine(this)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }
}
