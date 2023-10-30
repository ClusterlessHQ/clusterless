/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.show;

import clusterless.cls.json.JSONUtil;
import clusterless.cls.model.Struct;
import clusterless.commons.collection.OrderedSafeMaps;
import clusterless.commons.naming.Label;
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
            names = "--output-path",
            arity = "1",
            description = "Write out the documentation.",
            hidden = true
    )
    Optional<String> output;
    @CommandLine.Option(
            names = "--template",
            arity = "1",
            description = "The documentation template to use.",
            hidden = true
    )
    Optional<String> template;
    @CommandLine.Option(
            names = "--name",
            arity = "1",
            description = "The documentation file name to use.",
            hidden = true
    )
    Optional<String> name;

    @CommandLine.Option(
            names = "--append",
            arity = "1",
            description = "Append to the file",
            hidden = true
    )
    boolean append = false;

    interface Handler {
        int handle(String name, Class<?> documentedClass, Class<? extends Struct> structClass);
    }

    @NotNull
    private static String createFileName(String name, boolean required) {
        return Label.of(name)
                .with(required ? "required" : null)
                .lowerHyphen()
                .replace(":", "-")
                .concat(".adoc");
    }

    protected static String getModel(Class<? extends Struct> modelClass, boolean required) {
        try {
            // todo: have provider return a model instance with default values for use as a template
            if (required) {
                return JSONUtil.writeRequiredAsPrettyStringSafe(modelClass.getConstructor().newInstance());
            }
            return JSONUtil.writeAsPrettyStringSafe(modelClass.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("unable to instantiate model: " + modelClass.getCanonicalName(), e);
        }
    }

    @NotNull
    protected abstract String elementType();

    protected String elementSubType() {
        return elementType();
    }

    protected abstract Collection<String> getNames();

    protected Integer handleList() {
        Collection<String> ordered = getNames();

        if (output.isPresent()) {
            writeFromTemplate(ordered);
        } else {
            showCommand.main.printer().println(ordered);
        }

        return 0;
    }

    protected void writeFromTemplate(Collection<String> ordered) {
        try {
            Path path = Paths.get(output.get());
            path.toFile().mkdirs();

            File file = path
                    .resolve(name.orElse("nav.adoc"))
                    .toFile();

            Writer writer = new FileWriter(file, append);

            List<Map<String, String>> elements = new ArrayList<>();

            ordered.forEach(c -> elements.add(Map.of(
                    "name", c,
                    "filename", BaseShowElements.createFileName(c, false)
            )));

            Map<String, Object> params = OrderedSafeMaps.of(
                    "title", !append ? elementType() : null,
                    "component", elementSubType(),
                    "type", elementType().toLowerCase(),
                    "elements", elements
            );

            String template = elementType().equals(elementSubType()) ? "elements-list-adoc" : "elements-sublist-adoc";
            String partial = this.template.orElse(template); // a generic template
            showCommand.main.printer().writeWithTemplate("/templates/" + partial, params, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Integer handleDescribeAll() {
        for (String component : getNames()) {
            handle(component, this::printDescription);
        }
        return 0;
    }

    @Override
    protected Integer handleModelAll() throws Exception {
        for (String component : getNames()) {
            handle(component, this::printModel);
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
        try {
            Writer writer = showCommand.main.printer().writer();

            if (output.isPresent()) {
                Path path = Paths.get(output.get()).resolve("examples");
                path.toFile().mkdirs();

                File file = path
                        .resolve(BaseShowElements.createFileName(name, required.orElse(false)))
                        .toFile();

                writer = new FileWriter(file);
            }

            writer.append(getModel(modelClass, required.orElse(false)));
            writer.append("\n");

            writer.flush();
            writer.close();

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

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
                        .resolve(BaseShowElements.createFileName(name, false))
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
