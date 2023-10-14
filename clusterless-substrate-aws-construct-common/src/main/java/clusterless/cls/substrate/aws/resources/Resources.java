/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.resources;

import clusterless.cls.naming.Label;
import clusterless.cls.naming.Region;
import clusterless.cls.naming.Stage;
import clusterless.cls.naming.Version;
import clusterless.cls.substrate.aws.managed.ManagedProject;
import clusterless.cls.substrate.aws.managed.StagedApp;
import software.amazon.awscdk.Stack;
import software.constructs.Construct;

import java.util.Objects;

public class Resources {
    public static String globallyUniqueName(Construct scope, String name) {
        Objects.requireNonNull(name, "name may not be null");

        Label region = Region.of(Stack.of(scope).getRegion());
        Label stage = StagedApp.stagedOf(scope).stage();

        return stage.upperOnly()
                .with(name)
                .with(region)
                .lowerHyphen();
    }

    public static String globallyUniqueProjectName(Construct scope, String name) {
        Objects.requireNonNull(name, "name may not be null");

        Label region = Region.of(Stack.of(scope).getRegion());
        Label stage = StagedApp.stagedOf(scope).stage();
        Label project = ManagedProject.projectOf(scope).name();
        Label version = Version.of(ManagedProject.projectOf(scope).version());

        return stage.upperOnly()
                .with(project)
                .with(name)
                .with(version)
                .with(region)
                .lowerHyphen();
    }

    public static String regionallyUniqueName(Construct scope, String name) {
        return regionallyUniqueLabel(scope, Label.of(name), null).lowerHyphen();
    }

    public static Label regionallyUniqueLabel(Construct scope, Label name, Label qualifier) {
        Objects.requireNonNull(name, "name may not be null");

        Stage stage = StagedApp.stagedOf(scope).stage();

        return stage.upperOnly()
                .with(name)
                .with(qualifier);
    }

    public static String regionallyUniqueProjectName(Construct scope, String name) {
        Label label = regionallyUniqueProjectLabel(scope, Label.of(name));

        return label
                .lowerHyphen();
    }

    public static Label regionallyUniqueProjectLabel(Construct scope, Label name) {
        return regionallyUniqueProjectLabel(scope, name, null);
    }

    public static Label regionallyUniqueProjectLabel(Construct scope, Label name, Label qualifier) {
        Objects.requireNonNull(name, "name may not be null");

        Label stage = StagedApp.stagedOf(scope).stage();
        Label project = ManagedProject.projectOf(scope).name();
        Label version = Version.of(ManagedProject.projectOf(scope).version());

        return stage.upperOnly()
                .with(project)
                .with(name)
                .with(version)
                .with(qualifier);
    }
}
