/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.managed;

import clusterless.util.OrderedMaps;
import software.amazon.awscdk.App;
import software.amazon.awscdk.AppProps;
import software.amazon.awscdk.TagProps;
import software.amazon.awscdk.Tags;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class ManagedProject extends App {
    String name;
    String version;


    public static ManagedProject projectOf(Construct scope) {
        return (ManagedProject) scope.getNode().getRoot();
    }

    public ManagedProject() {
        super(AppProps.builder()
                .context(OrderedMaps.of(
                        "project", null,
                        "version", null
                ))
                .build());

//        applyTags();
    }

    protected void applyTags() {

        // apply tags to all constructs
        applyTags(OrderedMaps.of(
                "tag:prefix:project", null,
                "tag:prefix:version", null
        ), TagProps.builder()
                .applyToLaunchedInstances(true)
                .priority(100)
                .build());

        // apply tags only to the stack constructs
        applyTags(OrderedMaps.of(
                "tag:prefix:commit", null
        ), TagProps.builder()
                .includeResourceTypes(List.of(
                        "aws:ckd:stack",
                        "AWS::CloudFormation::Stack"
                ))
                .applyToLaunchedInstances(true)
                .priority(100)
                .build());
    }

    private void applyTags(Map<String, String> tagMap, TagProps tagProps) {
        Tags tags = Tags.of(this);

        tagMap.forEach((key, value) -> tags.add(key, value, tagProps));
    }
}
