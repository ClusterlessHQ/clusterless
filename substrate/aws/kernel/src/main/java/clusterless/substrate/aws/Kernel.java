/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws;

import clusterless.substrate.SubstrateProvider;
import picocli.CommandLine;

import java.lang.reflect.Method;
import java.util.List;

/**
 *
 */
@CommandLine.Command(mixinStandardHelpOptions = true, subcommands = {
        Manage.class,
        Report.class
})
public class Kernel implements SubstrateProvider {
    public static void main(String[] args) {
        System.exit(new Kernel().execute(args));
    }

    @CommandLine.Option(names = "--direct", arity = "0..1")
    public boolean direct = false;

    public Kernel() {
    }

    @Override
    public String name() {
        return "aws";
    }

    @Override
    public int execute(String[] args) {
        List<Method> verify = CommandLine.getCommandMethods(Manage.class, "verify");
        return new CommandLine(this)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(args);
    }
}
