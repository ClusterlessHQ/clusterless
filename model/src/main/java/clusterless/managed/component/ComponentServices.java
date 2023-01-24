/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.component;

import clusterless.model.Model;

import java.util.*;

/**
 *
 */
public class ComponentServices {
    public static final ComponentServices INSTANCE = new ComponentServices();

    private final EnumMap<ComponentType, Map<String, ComponentService<ComponentContext, Model>>> componentServices = new EnumMap<>(ComponentType.class);

    protected ComponentServices() {
        ServiceLoader<ComponentService> serviceLoader = ServiceLoader.load(ComponentService.class);

        serviceLoader.stream().forEach(s -> {
            ProvidesComponent annotation = s.type().getAnnotation(ProvidesComponent.class);
            ComponentType type = annotation.type();
            String name = annotation.name();
            ComponentService<ComponentContext, Model> componentService = s.get();

            componentServices.computeIfAbsent(type, k -> new HashMap<>()).put(name, componentService);
        });
    }

    public EnumMap<ComponentType, Map<String, ComponentService<ComponentContext, Model>>> componentServices() {
        return componentServices;
    }

    public Collection<String> names(ComponentType type) {
        return componentServices.get(type).keySet();
    }

    public <M extends Model> Optional<ComponentService<ComponentContext, M>> get(ComponentType type, String name) {
        ComponentService<ComponentContext, M> componentContextModelComponentService = (ComponentService<ComponentContext, M>) componentServices.get(type).get(name);
        return Optional.ofNullable(componentContextModelComponentService);
    }
}
