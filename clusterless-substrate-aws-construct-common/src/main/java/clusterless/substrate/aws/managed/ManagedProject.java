/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.model.deploy.Deployable;
import clusterless.naming.Label;
import clusterless.naming.Stage;
import clusterless.substrate.aws.util.TagsUtil;
import clusterless.util.OrderedMaps;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.TagProps;
import software.constructs.Construct;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class ManagedProject extends StagedApp implements Managed {
    private final Label name;
    private final String version;
    private final List<Deployable> deployableModel;

    private final List<ManagedStack> stacks = new LinkedList<>();

    public static ManagedProject projectOf(Construct scope) {
        return (ManagedProject) scope.getNode().getRoot();
    }

    public ManagedProject(String name, String version, String stage, List<Deployable> deployableModels) {
        super(AppProps.builder()
                        .context(OrderedMaps.of(
                                "project", Label.of(name).lowerHyphen(),
                                "version", version,
                                "stage", Label.of(stage).upperOnly()
                        ))
                        .build(),
                Stage.of(stage)
        );

        this.name = Label.of(name);
        this.version = version;
        this.deployableModel = deployableModels;

        applyTags();
    }

    public List<Deployable> projectModels() {
        return deployableModel;
    }

    public Label name() {
        return name;
    }

    public String version() {
        return version;
    }

    public List<ManagedStack> stacks() {
        return stacks;
    }

    public ManagedProject addStack(ManagedStack managedStack) {
        stacks.add(managedStack);
        return this;
    }

    protected void applyTags() {
        // apply tags to all constructs
        TagsUtil.applyTags(this, OrderedMaps.of(
                "cls:project:name", name().lowerHyphen(),
                "cls:project:version", version(),
                "cls:project:stage", stage().camelCase()
        ), TagProps.builder()
                .applyToLaunchedInstances(true)
                .priority(100)
                .build());

        // apply tags only to the stack constructs
//        TagsUtil.applyTags(this, OrderedMaps.of(
//                "tag:prefix:commit", "none"
//        ), TagProps.builder()
//                .includeResourceTypes(List.of(
//                        "aws:ckd:stack",
//                        "AWS::CloudFormation::Stack"
//                ))
//                .applyToLaunchedInstances(true)
//                .priority(100)
//                .build());
    }
}
