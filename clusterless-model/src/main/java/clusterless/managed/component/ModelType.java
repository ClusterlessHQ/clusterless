/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.component;

import clusterless.util.Label;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public enum ModelType implements Label.EnumLabel {
    Resource(clusterless.model.Resource.class),
    Boundary(clusterless.model.Boundary.class),
    Process(clusterless.model.Process.class);

    static Map<Class, ModelType> types = new LinkedHashMap<>();

    static {
        for (ModelType value : ModelType.values()) {
            types.put(value.modelClass(), value);
        }
    }

    final Class modelClass;

    ModelType(Class modelClass) {
        this.modelClass = modelClass;
    }

    public Class modelClass() {
        return modelClass;
    }

    public static ModelType find(Class type) {
        return types.get(type);
    }

    public static ModelType[] values(ModelType... modelTypes) {
        return modelTypes;
    }
}
