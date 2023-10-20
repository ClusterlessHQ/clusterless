/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import clusterless.commons.naming.Label;
import clusterless.commons.naming.Ref;
import clusterless.commons.naming.Region;
import clusterless.commons.substrate.aws.cdk.scoped.ScopedApp;
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
        Ref ref = ClsBootstrap.bootstrapBase(scope, Ref.nameRef())
                .withResourceType(EVENT_BUS)
                .withResourceName(ARC_EVENT_BUS);

        return Fn.importValue(ref.exportName());
    }

    private static String eventBusName(@NotNull Construct scope, String name) {
        Label region = Region.of(Stack.of(scope).getRegion());
        Label stage = ScopedApp.scopedOf(scope).stage();
        return stage.upperOnly()
                .with(name)
                .with(region)
                .lowerHyphen();
    }
}
