/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless;

import clusterless.managed.component.Component;
import clusterless.managed.component.ComponentContext;
import clusterless.managed.component.ComponentService;
import clusterless.managed.component.ProvidesComponent;
import clusterless.model.Model;
import clusterless.model.Struct;
import clusterless.substrate.SubstrateProvider;
import clusterless.util.Annotations;
import clusterless.util.ExitCodeException;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.Writer;
import java.util.*;
import java.util.function.Predicate;

@CommandLine.Command(
        name = "component",
        description = "Show all available provider components (resources, barriers, boundaries, and arcs)."
)
public class ShowComponents extends BaseShowElements {

    public ShowComponents() {
    }

    @Override
    @NotNull
    protected String elementType() {
        return "Components";
    }

    @Override
    protected Collection<String> getNames() {
        return getNamesHaving(e -> true);
    }

    @NotNull
    protected Set<String> getNamesHaving(Predicate<Map.Entry<String, Class<? extends Struct>>> entryPredicate) {
        Map<String, SubstrateProvider> providers = showCommand.main.substratesOptions().requestedProvider();
        Set<String> ordered = new TreeSet<>();

        for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
            Map<String, Class<? extends Struct>> models = entry.getValue()
                    .models();

            models.entrySet()
                    .stream()
                    .filter(entryPredicate)
                    .map(Map.Entry::getKey)
                    .forEach(ordered::add);
        }

        return ordered;
    }

    protected int handle(String name, Handler func) {
        Map<String, SubstrateProvider> providers = showCommand.main.substratesOptions().requestedProvider();
        for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
            ComponentService<ComponentContext, Model, Component> componentService = entry.getValue().components().get(name);

            if (componentService != null) {
                return func.handle(name, componentService.getClass(), componentService.modelClass());
            }
        }

        throw new ExitCodeException("no model found for: " + name, 1);
    }

    protected void printDescriptionUsing(Class<?> documentedClass, Class<? extends Struct> modelClass, String template, Writer writer) {
        Optional<ProvidesComponent> providesComponent = Annotations.find(documentedClass, ProvidesComponent.class);

        if (providesComponent.isEmpty()) {
            throw new IllegalStateException("component does not have a ProvidesComponent annotation: " + documentedClass.getName());
        }

        Map<String, Object> params = Map.of(
                "name", providesComponent.get().type(),
                "component", elementSubType(),
                "synopsis", providesComponent.get().synopsis(),
                "description", providesComponent.get().description(),
                "model", BaseShowElements.getModel(modelClass)
        );

        showCommand.main.printer().writeWithTemplate(template, params, writer);
    }
}
