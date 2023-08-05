/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless;

import clusterless.json.JSONUtil;
import clusterless.model.Struct;
import clusterless.naming.Label;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class BaseShowElements extends ShowCommand.BaseShow {
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

    interface Handler {
        int handle(String name, Class<?> documentedClass, Class<? extends Struct> structClass);
    }

    @NotNull
    private static String createFileName(String name) {
        return Label.of(name)
                .lowerHyphen()
                .replace(":", "-")
                .concat(".adoc");
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

    @NotNull
    protected abstract String elementType();

    protected abstract Collection<String> getNames();

    protected Integer handleList() {
        Collection<String> ordered = getNames();

        if (output.isPresent()) {
            try {
                Path path = Paths.get(output.get());
                path.toFile().mkdirs();

                File file = path
                        .resolve(name.orElse("nav.adoc"))
                        .toFile();

                Writer writer = new FileWriter(file);

                List<Map<String, String>> elements = new ArrayList<>();

                ordered.forEach(c -> elements.add(Map.of(
                        "name", c,
                        "filename", BaseShowElements.createFileName(c)
                )));

                Map<String, Object> params = Map.of(
                        "title", elementType(),
                        "type", elementType().toLowerCase(),
                        "elements", elements
                );

                String partial = template.orElse("elements-list-adoc"); // a generic template
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
        for (String component : getNames()) {
            handle(component, this::printDescription);
        }
        return 0;
    }

    protected Integer handleModel() {
        return handle(exclusive.model.orElseThrow(), this::printModel);
    }

    @Override
    protected Integer handleDescribe() {
        return handle(exclusive.name.orElseThrow(), this::printDescription);
    }

    protected abstract int handle(String name, Handler func);

    protected int printModel(String name, Class<?> documentedClass, Class<? extends Struct> modelClass) {
        showCommand.main.printer().println(getModel(modelClass));
        return 0;
    }

    protected int printDescription(String name, Class<?> documentedClass, Class<? extends Struct> modelClass) {
        try {
            Writer writer = showCommand.main.printer().writer();
            String template = "/templates/" + elementType().toLowerCase() + "-cli";

            if (output.isPresent()) {
                template = "/templates/" + elementType().toLowerCase() + "-adoc";
                Path path = Paths.get(output.get()).resolve("pages");
                path.toFile().mkdirs();

                File file = path
                        .resolve(BaseShowElements.createFileName(name))
                        .toFile();

                writer = new FileWriter(file);
            }

            printDescriptionUsing(documentedClass, modelClass, template, writer);

            writer.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return 0;
    }

    protected abstract void printDescriptionUsing(Class<?> documentedClass, Class<? extends Struct> modelClass, String template, Writer writer);
}
