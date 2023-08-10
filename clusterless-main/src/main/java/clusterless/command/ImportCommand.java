/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.command;

import clusterless.CommandWrapper;
import picocli.CommandLine;

@CommandLine.Command(
        hidden = true,
        name = "import",
        description = "import resources into a project from a declared placement",
        footer = """
                This feature is not yet functional as it's based on `cdk import` which is still in preview.
                https://github.com/aws/aws-cdk/blob/v1-main/packages/aws-cdk/README.md#cdk-import
                """
)
public class ImportCommand extends CommandWrapper {
    public ImportCommand() {
        super(new ImportCommandOptions());
    }
}