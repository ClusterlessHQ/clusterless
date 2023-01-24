/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public enum ComponentType {
    Resource(clusterless.model.Resource.class),
    Boundary(clusterless.model.Boundary.class),
    Process(clusterless.model.Process.class),
    Arc(clusterless.model.Arc.class);

    static Map<Class, ComponentType> types = new LinkedHashMap<>();

    static {
        for (ComponentType value : ComponentType.values()) {
            types.put(value.modelType(), value);
        }
    }

    final Class modelType;

    ComponentType(Class modelType) {
        this.modelType = modelType;
    }

    public Class modelType() {
        return modelType;
    }

    public static ComponentType find(Class type) {
        return types.get(type);
    }
}
