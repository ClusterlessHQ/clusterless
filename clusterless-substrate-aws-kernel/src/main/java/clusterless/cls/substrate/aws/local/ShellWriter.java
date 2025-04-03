/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.local;

import clusterless.cls.managed.component.ExecCommand;
import clusterless.commons.util.Runtimes;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ShellWriter {
    private final Runtimes.Runtime runtime;

    public ShellWriter(Runtimes.Runtime runtime) {
        this.runtime = runtime;
    }

    public String toScript(List<ExecCommand> commands, String dockerImage, String entryPoint) {
        StringBuilder buffer = new StringBuilder();

        buffer.append("#!/bin/bash\n");

        for (ExecCommand command : commands) {
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

            // insert docker voodoo
            if (dockerImage != null) {
                buffer.append("\n\n# \"assume -x\" will export all env vars\n");
                buffer.append("docker run -it --rm \\\n");
                buffer.append("  -e AWS_REGION=\"$AWS_REGION\" \\\n");
                buffer.append("  -e AWS_ACCESS_KEY_ID=\"$AWS_ACCESS_KEY_ID\" \\\n");
                buffer.append("  -e AWS_SECRET_ACCESS_KEY=\"$AWS_SECRET_ACCESS_KEY\" \\\n");

                for (String key : command.environment().keySet()) {
                    buffer.append(String.format("  -e %s=\"$%s\" \\\n", key, key));
                }

                buffer.append("  ");
                buffer.append(dockerImage);
                buffer.append("\\\n  ");
            }

            if (entryPoint != null) {
                if (entryPoint.startsWith("../")) {
                    entryPoint = Path.of(entryPoint).toAbsolutePath().normalize().toString();
                }

                buffer.append(entryPoint);
                buffer.append(" ");
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
