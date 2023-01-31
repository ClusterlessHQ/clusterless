/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.startup.Startup;
import clusterless.substrate.SubstrateProvider;
import clusterless.substrate.aws.cdk.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.Arrays;

/**
 *
 */
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {
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
    public static void main(String[] args) {
        System.exit(new Kernel().execute(args));
    }

    @Override
    public String substrate() {
        return "aws";
    }

    @Override
    public int execute(String[] args) {
        LOG.info("kernel: {} ", Arrays.toString(args));

        return new CommandLine(this)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }
}
