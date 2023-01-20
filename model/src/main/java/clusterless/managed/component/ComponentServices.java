/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.component;

import java.util.*;

/**
 *
 */
public class ComponentServices {

    private final EnumMap<ComponentType, Map<String, ComponentService<ComponentProps>>> componentServices = new EnumMap<>(ComponentType.class);

    public ComponentServices() {

        ServiceLoader<ComponentService> serviceLoader = ServiceLoader.load(ComponentService.class);

        serviceLoader.stream().forEach(s -> {
            ProvidesComponent annotation = s.type().getAnnotation(ProvidesComponent.class);
            ComponentType type = annotation.type();
            String name = annotation.name();
            ComponentService<ComponentProps> componentService = s.get();

            componentServices.computeIfAbsent(type, k -> new HashMap<>()).put(name, componentService);
        });
    }

    public Collection<String> names(ComponentType type) {
        return componentServices.get(type).keySet();
    }

    public Optional<ComponentService<ComponentProps>> get(ComponentType type, String name) {
        return Optional.ofNullable(componentServices.get(type).get(name));
    }
}
