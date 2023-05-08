/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.resources;

import clusterless.naming.ExportRef;
import clusterless.naming.Label;
import clusterless.naming.Region;
import clusterless.substrate.aws.managed.StagedApp;
import org.jetbrains.annotations.NotNull;
import software.amazon.awscdk.Fn;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

/**
 *
 */
public class Events {
    public static final Label ARC_EVENT_BUS = Label.of("ArcEventBus");
    public static final String EVENT_BUS = "eventBus";

    public static String arcEventBusName(@NotNull Construct scope) {
        return eventBusName(scope, "ArcEvents");
    }

    public static String arcEventBusNameRef(@NotNull Construct scope) {
        ExportRef ref = ClsBootstrap.bootstrapBase(scope)
                .withQualifier(ExportRef.ExportQualifier.Name)
                .withResourceType(EVENT_BUS)
                .withResourceName(ARC_EVENT_BUS);

        return Fn.importValue(ref.exportName());
    }
    private static String eventBusName(@NotNull Construct scope, String name) {
        Label region = Region.of(Stack.of(scope).getRegion());
        Label stage = StagedApp.stagedOf(scope).stage();
        return stage.upperOnly()
                .with(name)
                .with(region)
                .lowerHyphen();
    }
}
