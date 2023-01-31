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
@CommandLine.Command(description = "destroy project declared by the project files")
public class DestroyCommandOptions extends LifecycleCommandOptions {
}
