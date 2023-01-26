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

    private final EnumMap<ManagedType, EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model>>>> componentServices;

    {
        componentServices = new EnumMap<>(ManagedType.class);

        Arrays.stream(ManagedType.values()).forEach(k -> componentServices.put(k, new EnumMap<>(ModelType.class)));

    }

    protected ComponentServices() {
        ServiceLoader<ComponentService> serviceLoader = ServiceLoader.load(ComponentService.class);

        serviceLoader.stream().forEach(s -> {
            ProvidesComponent annotation = s.type().getAnnotation(ProvidesComponent.class);
            ManagedType managedType = annotation.managedType();
            ModelType model = annotation.modelType();
            String name = annotation.name();

            LOG.info("loading component service provider: {} {} {}", managedType, model, name);

            ComponentService<ComponentContext, Model> componentService = s.get();

            componentServices.get(managedType).computeIfAbsent(model, k -> new HashMap<>())
                    .put(name, componentService);
        });
    }

    public Map<String, ComponentService<ComponentContext, Model>> componentServicesFor(ModelType modelType) {
        Map<String, ComponentService<ComponentContext, Model>> result = new HashMap<>();

        for (ManagedType managedType : ManagedType.values()) {
            if (componentServices.containsKey(managedType)) {
                if (componentServices.get(managedType).containsKey(modelType)) {
                    result.putAll(componentServices.get(managedType).get(modelType));
                }
            }
        }

        return result;
    }

    public EnumMap<ModelType, Map<String, ComponentService<ComponentContext, Model>>> componentServicesFor(ManagedType managedType) {
        return componentServices.get(managedType);
    }

    public <M extends Model> Optional<ComponentService<ComponentContext, M>> get(ManagedType managedType, ModelType modelType, String name) {
        Map<String, ComponentService<ComponentContext, Model>> serviceMap = componentServices.get(managedType).get(modelType);

        if (serviceMap == null) {
            throw new IllegalStateException("model map for " + managedType + " " + modelType + "  is missing");
        }

        ComponentService<ComponentContext, M> componentContextModelComponentService = (ComponentService<ComponentContext, M>) serviceMap.get(name);
        return Optional.ofNullable(componentContextModelComponentService);
    }

}
