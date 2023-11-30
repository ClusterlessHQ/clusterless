/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.lifecycle;

import clusterless.cls.managed.ModelType;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class StackGroups {
    @NotNull
    public List<ModelType[]> independentModels() {
        List<ModelType[]> stackGroups = new LinkedList<>();

        // each instance result in one stack, but this controls the order of the stacks
        stackGroups.add(ModelType.values(ModelType.Resource));
        stackGroups.add(ModelType.values(ModelType.Activity));
        stackGroups.add(ModelType.values(ModelType.Boundary));

        return stackGroups;
    }

    public List<ModelType[]> groupedModels() {
        List<ModelType[]> stackGroups = new LinkedList<>();

        // places these types in the same stack
        stackGroups.add(ModelType.values(ModelType.Resource, ModelType.Activity, ModelType.Boundary));

        return stackGroups;
    }

    public List<ModelType[]> managedModels() {
        List<ModelType[]> stackGroups = new LinkedList<>();

        // each of these is embedded in a managed stack per type
        stackGroups.add(ModelType.values(ModelType.Arc));

        return stackGroups;
    }
}
