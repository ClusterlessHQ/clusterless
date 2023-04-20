/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed;

import clusterless.managed.component.ArcComponent;
import clusterless.managed.component.BoundaryComponent;
import clusterless.managed.component.Component;
import clusterless.managed.component.ResourceComponent;
import clusterless.model.Model;
import clusterless.naming.Label;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public enum ModelType implements Label.EnumLabel {
    Resource(
            clusterless.model.deploy.Resource.class,
            ResourceComponent.class
    ),
    Boundary(
            clusterless.model.deploy.Boundary.class,
            BoundaryComponent.class
    ),
    Arc(
            clusterless.model.deploy.Arc.class,
            ArcComponent.class
    );

    static final Map<Class<?>, ModelType> types = new LinkedHashMap<>();

    static {
        for (ModelType value : ModelType.values()) {
            types.put(value.modelClass(), value);
        }
    }

    final Class<? extends Model> modelClass;
    final Class<? extends Component> componentClass;

    ModelType(Class<? extends Model> modelClass, Class<? extends Component> componentClass) {
        this.modelClass = modelClass;
        this.componentClass = componentClass;
    }

    public Class<? extends Model> modelClass() {
        return modelClass;
    }

    public Class<? extends Component> componentClass() {
        return componentClass;
    }

    public static ModelType findFromModel(Class<?> type) {
        if (types.containsKey(type)) {
            return types.get(type);
        }

        for (Map.Entry<Class<?>, ModelType> entry : types.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static ModelType[] values(ModelType... modelTypes) {
        return modelTypes;
    }
}
