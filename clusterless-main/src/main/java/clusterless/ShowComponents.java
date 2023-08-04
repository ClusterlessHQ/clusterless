/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless;

import clusterless.json.JSONUtil;
import clusterless.managed.component.Component;
import clusterless.managed.component.ComponentContext;
import clusterless.managed.component.ComponentService;
import clusterless.managed.component.ProvidesComponent;
import clusterless.model.Model;
import clusterless.model.Struct;
import clusterless.naming.Label;
import clusterless.substrate.SubstrateProvider;
import clusterless.util.Annotations;
import clusterless.util.ExitCodeException;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@CommandLine.Command(
        name = "component"
)
public class ShowComponents extends ShowCommand.BaseShow {

    interface Handler {
        int handle(String name, ComponentService<ComponentContext, Model, Component> service, Class<? extends Struct> structClass);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @CommandLine.Option(
            names = "--output",
            arity = "1",
            description = "write out the documentation",
            hidden = true
    )
    Optional<String> output;

    @CommandLine.Option(
            names = "--template",
            arity = "1",
            description = "the documentation template to use",
            hidden = true
    )
    Optional<String> template;

    @CommandLine.Option(
            names = "--name",
            arity = "1",
            description = "the documentation file name to use",
            hidden = true
    )
    Optional<String> name;

    public ShowComponents() {
    }

    protected Integer handleList() {
        Map<String, SubstrateProvider> providers = showCommand.main.substratesOptions().requestedProvider();

        Set<String> ordered = new TreeSet<>();

        for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
            ordered.addAll(entry.getValue().models().keySet());
        }

        if (output.isPresent()) {
            try {
                Path path = Paths.get(output.get());
                path.toFile().mkdirs();

                File file = path
                        .resolve(name.orElse("nav.adoc"))
                        .toFile();

                Writer writer = new FileWriter(file);

                List<Map<String, String>> components = new ArrayList<>();

                ordered.forEach(c -> components.add(Map.of(
                        "name", c,
                        "filename", createFileName(c)
                )));

                Map<String, Object> params = Map.of(
                        "title", "Components",
                        "components", components
                );

                String partial = template.orElse("components-list-adoc");
                showCommand.main.printer().writeWithTemplate("/templates/" + partial, params, writer);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            showCommand.main.printer().println(ordered);
        }

        return 0;
    }

    @Override
    protected Integer handleDescribeAll() {
        Map<String, SubstrateProvider> providers = showCommand.main.substratesOptions().requestedProvider();

        for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
            Set<String> names = new TreeSet<>(entry.getValue().models().keySet());
            for (String component : names) {
                handle(component, this::printDescription);
            }
        }
        return 0;
    }

    protected Integer handleTemplate() {
        return handle(exclusive.template.orElseThrow(), this::printModel);
    }

    @Override
    protected Integer handleDescribe() {
        return handle(exclusive.component.orElseThrow(), this::printDescription);
    }

    private int handle(String name, Handler func) {
        Map<String, SubstrateProvider> providers = showCommand.main.substratesOptions().requestedProvider();
        for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
            ComponentService<ComponentContext, Model, Component> componentService = entry.getValue().components().get(name);

            if (componentService != null) {
                return func.handle(name, componentService, componentService.modelClass());
            }
        }

        throw new ExitCodeException("no model found for: " + name, 1);
    }

    protected int printModel(String name, ComponentService<ComponentContext, Model, Component> componentService, Class<? extends Struct> modelClass) {
        showCommand.main.printer().println(getModel(modelClass));
        return 0;
    }

    protected int printDescription(String name, ComponentService<ComponentContext, Model, Component> componentService, Class<? extends Struct> modelClass) {
        try {
            Writer writer = showCommand.main.printer().writer();
            String template = "/templates/components-cli";

            if (output.isPresent()) {
                template = "/templates/components-adoc";
                Path path = Paths.get(output.get()).resolve("pages");
                path.toFile().mkdirs();

                File file = path
                        .resolve(createFileName(name))
                        .toFile();

                writer = new FileWriter(file);
            }

            printDescriptionUsing(componentService, modelClass, template, writer);

            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return 0;
    }

    @NotNull
    private static String createFileName(String name) {
        return Label.of(name)
                .lowerHyphen()
                .replace(":", "-")
                .concat(".adoc");
    }

    protected void printDescriptionUsing(ComponentService<ComponentContext, Model, Component> componentService, Class<? extends Struct> modelClass, String template, Writer writer) {
        Class<?> componentClass = componentService.getClass();
        Optional<ProvidesComponent> providesComponent = Annotations.find(componentClass, ProvidesComponent.class);

        if (providesComponent.isEmpty()) {
            throw new IllegalStateException("component does not have a ProvidesComponent annotation: " + componentClass.getName());
        }

        Map<String, Object> params = Map.of(
                "name", providesComponent.get().type(),
                "synopsis", providesComponent.get().synopsis(),
                "description", providesComponent.get().description(),
                "model", getModel(modelClass)
        );

        showCommand.main.printer().writeWithTemplate(template, params, writer);
    }

    protected static String getModel(Class<? extends Struct> modelClass) {
        String model;
        try {
            // todo: have provider return a model instance with default values for use as a template
            model = JSONUtil.writeAsPrettyStringSafe(modelClass.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return model;
    }
}
