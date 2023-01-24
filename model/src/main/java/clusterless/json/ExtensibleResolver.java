/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.json;

import clusterless.managed.component.ComponentContext;
import clusterless.managed.component.ComponentService;
import clusterless.managed.component.ComponentServices;
import clusterless.managed.component.ComponentType;
import clusterless.model.Model;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

import java.util.Map;

/**
 *
 */
public class ExtensibleResolver extends TypeIdResolverBase {

    private Map<String, ComponentService<ComponentContext, Model>> serviceMap;

    public ExtensibleResolver() {
    }

    @Override
    public void init(JavaType javaType) {
        ComponentServices componentServices = ComponentServices.INSTANCE;

        Class<?> rawClass = javaType.getRawClass();
        ComponentType componentType = ComponentType.find(rawClass);

        if (componentType == null) {
            throw new IllegalStateException("unknown component type for: " + rawClass.getName());
        }

        serviceMap = componentServices.componentServices().get(componentType);
    }

    @Override
    public String idFromValue(Object value) {
        return null;
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return null;
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.NAME;
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        return context.getTypeFactory().constructType(serviceMap.get(id).modelType());
    }
}
