/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

import picocli.CommandLine;

import java.util.Stack;

public class DashedParameterConsumer implements CommandLine.IParameterConsumer {
    public DashedParameterConsumer() {
    }

    @Override
    public void consumeParameters(Stack<String> args, CommandLine.Model.ArgSpec argSpec, CommandLine.Model.CommandSpec commandSpec) {
        String arg = args.pop();
        argSpec.setValue(arg);
    }
}
