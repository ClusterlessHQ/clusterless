/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.command;

import picocli.CommandLine;

/**
 *
 */
@CommandLine.Command(description = "diff the given project files with the deployed project")
public class DiffCommandOptions extends ProjectCommandOptions {
}
