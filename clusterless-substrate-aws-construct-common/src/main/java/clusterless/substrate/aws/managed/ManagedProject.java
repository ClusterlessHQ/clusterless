/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.managed.Label;
import clusterless.model.Project;
import clusterless.substrate.aws.util.TagsUtil;
import clusterless.util.OrderedMaps;
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.TagProps;
import software.constructs.Construct;

import java.util.List;

/**
 *
 */
public class ManagedProject extends App implements Managed {
    private final Label name;
    private final String version;
    private final Project projectModel;


    public static ManagedProject projectOf(Construct scope) {
        return (ManagedProject) scope.getNode().getRoot();
    }

    public ManagedProject(Project projectModel) {
        super(AppProps.builder()
                .context(OrderedMaps.of(
                        "project", Label.of(projectModel.name()).lowerHyphen(),
                        "version", projectModel.version()
                ))
                .build());

        this.name = Label.of(projectModel.name());
        this.version = projectModel.version();
        this.projectModel = projectModel;

        applyTags();
    }

    public Project projectModel() {
        return projectModel;
    }

    @Override
    public Label baseId() {
        return name();
    }

    public Label name() {
        return name;
    }

    public String version() {
        return version;
    }

    protected void applyTags() {
        // apply tags to all constructs
        TagsUtil.applyTags(this, OrderedMaps.of(
                "tag:prefix:project", name().lowerHyphen(),
                "tag:prefix:version", version
        ), TagProps.builder()
                .applyToLaunchedInstances(true)
                .priority(100)
                .build());

        // apply tags only to the stack constructs
        TagsUtil.applyTags(this, OrderedMaps.of(
                "tag:prefix:commit", "none"
        ), TagProps.builder()
                .includeResourceTypes(List.of(
                        "aws:ckd:stack",
                        "AWS::CloudFormation::Stack"
                ))
                .applyToLaunchedInstances(true)
                .priority(100)
                .build());
    }
}
