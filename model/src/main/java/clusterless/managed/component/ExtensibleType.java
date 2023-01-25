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
public enum ExtensibleType {
    Resource(clusterless.model.Resource.class),
    Boundary(clusterless.model.Boundary.class),
    Process(clusterless.model.Process.class);

    static Map<Class, ExtensibleType> types = new LinkedHashMap<>();

    static {
        for (ExtensibleType value : ExtensibleType.values()) {
            types.put(value.modelClass(), value);
        }
    }

    final Class modelClass;

    ExtensibleType(Class modelClass) {
        this.modelClass = modelClass;
    }

    public Class modelClass() {
        return modelClass;
    }

    public static ExtensibleType find(Class type) {
        return types.get(type);
    }
}
