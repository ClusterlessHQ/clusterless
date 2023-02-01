/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.printer;

import picocli.CommandLine;

import java.io.PrintStream;
import java.util.Set;

/**
 *
 */
public class Printer {
    @CommandLine.Option(names = {"-j", "--json"}, description = "print results as json")
    boolean json = false;
    private PrintStream out = System.out;

    public Printer() {
    }

    public void print(Set<String> strings) {
        out.println(strings);
    }
}
