/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls;

import clusterless.cls.model.deploy.Arc;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.util.Collection;

@CommandLine.Command(
        name = "arc",
        description = "Show available provider arcs."
)
public class ShowArcs extends ShowComponents {
    @Override
    protected @NotNull String elementSubType() {
        return "Arcs";
    }

    @Override
    protected Collection<String> getNames() {
        return super.getNamesHaving(e -> Arc.class.isAssignableFrom(e.getValue()));
    }
}
