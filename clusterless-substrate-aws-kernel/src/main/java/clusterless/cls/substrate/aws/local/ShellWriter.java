/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.local;

import clusterless.cls.managed.component.ArcLocalExecutor;
import clusterless.cls.util.Runtimes;

import java.util.List;
import java.util.Map;

public class ShellWriter {
    private final Runtimes.Runtime runtime;

    public ShellWriter(Runtimes.Runtime runtime) {
        this.runtime = runtime;
    }

    public String toScript(List<ArcLocalExecutor.Command> commands) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("#!/bin/bash\n");

        for (ArcLocalExecutor.Command command : commands) {
            if (command.headerComment() != null) {
                writeMultiLineComment(buffer, command.headerComment());
            }

            for (Map.Entry<String, String> entry : command.environment().entrySet()) {
                if (command.environmentComments().containsKey(entry.getKey())) {
                    writeMultiLineComment(buffer, command.environmentComments().get(entry.getKey()));
                }
                buffer.append("export ");
                buffer.append(String.format("%s=%s", entry.getKey(), encode(entry.getValue())));
                buffer.append("\n");
            }

            if (command.commandComment() != null) {
                writeMultiLineComment(buffer, command.commandComment());
            }

            buffer.append(String.join(" ", command.command()));
            buffer.append("\n");
        }

        return buffer.toString();
    }

    private String encode(String value) {
        if (value.trim().startsWith("{") && value.trim().endsWith("}")) {
            return "'" + value.replaceAll("'", "\\\\'") + "'";
        }
        return value;
    }

    private static void writeMultiLineComment(StringBuilder buffer, String s) {
        for (String line : s.split("\n")) {
            buffer.append("# ");
            buffer.append(line);
            buffer.append("\n");
        }
    }
}
