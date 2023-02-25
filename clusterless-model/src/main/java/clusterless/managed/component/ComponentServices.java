/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.component;

import clusterless.model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 *
 */
public class ComponentServices {
    private static final Logger LOG = LogManager.getLogger(ComponentServices.class);

    public static final ComponentServices INSTANCE = new ComponentServices();

    private final EnumMap<Isolation, EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model, Component>>>> componentServices;

    {
        componentServices = new EnumMap<>(Isolation.class);
        Arrays.stream(Isolation.values()).forEach(k -> componentServices.put(k, new EnumMap<>(ModelType.class)));
    }

    protected ComponentServices() {
        ServiceLoader<ComponentService> serviceLoader = ServiceLoader.load(ComponentService.class);

        serviceLoader.stream().forEach(s -> {
            ProvidesComponent annotation = s.type().getAnnotation(ProvidesComponent.class);
            Isolation isolation = annotation.isolation();
            ModelType model = annotation.provides();
            String name = annotation.name();

            LOG.info("loading component service provider: {} {} {}", isolation, model, name);

            ComponentService<ComponentContext, Model, Component> componentService = s.get();

            componentServices.get(isolation).computeIfAbsent(model, k -> new HashMap<>())
                    .put(name, componentService);
        });
    }

    public Map<String, ComponentService<ComponentContext, Model, Component>> componentServices() {
        Map<String, ComponentService<ComponentContext, Model, Component>> result = new LinkedHashMap<>();

        for (ModelType modelType : ModelType.values()) {
            result.putAll(componentServicesFor(modelType));
        }

        return result;
    }

    public Map<String, ComponentService<ComponentContext, Model, Component>> componentServicesFor(ModelType modelType) {
        Map<String, ComponentService<ComponentContext, Model, Component>> result = new HashMap<>();

        for (Isolation isolation : Isolation.values()) {
            if (componentServices.containsKey(isolation)) {
                if (componentServices.get(isolation).containsKey(modelType)) {
                    result.putAll(componentServices.get(isolation).get(modelType));
                }
            }
        }

        return result;
    }

    public EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model, Component>>> componentServicesFor(Isolation isolation) {
        return componentServices.get(isolation);
    }
}
