/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.component;

import clusterless.managed.ModelType;
import clusterless.model.Model;
import clusterless.util.Annotations;
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
            Optional<DeclaresComponent> provides = Annotations.find(s.type(), DeclaresComponent.class);
            Optional<ProvidesComponent> typeOf = Annotations.find(s.type(), ProvidesComponent.class);
            Isolation isolation = provides.orElseThrow().isolation();
            ModelType model = provides.orElseThrow().provides();
            String type = typeOf.orElseThrow().value();

            LOG.info("loading component service provider: {} {} {}", isolation, model, type);

            ComponentService<ComponentContext, Model, Component> componentService = s.get();

            componentServices.get(isolation).computeIfAbsent(model, k -> new HashMap<>())
                    .put(type, componentService);
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
