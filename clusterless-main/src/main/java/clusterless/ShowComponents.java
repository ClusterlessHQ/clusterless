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
import clusterless.substrate.SubstrateProvider;
import clusterless.util.Annotations;
import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.helper.StringHelpers;
import picocli.CommandLine;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import static com.github.jknack.handlebars.internal.lang3.Validate.notNull;

@CommandLine.Command(
        name = "component"
)
public class ShowComponents extends ShowCommand.BaseShow {

    public ShowComponents() {
    }

    protected Integer handleList() {
        Map<String, SubstrateProvider> providers = showCommand.main.substratesOptions().requestedProvider();

        for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
            showCommand.main.printer().println(entry.getValue().models().keySet());
        }

        return 0;
    }

    @Override
    protected Integer handleDescribeAll() {
        Map<String, SubstrateProvider> providers = showCommand.main.substratesOptions().requestedProvider();

        boolean first = true;
        for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
            Set<String> names = new TreeSet<>(entry.getValue().models().keySet());
            for (String component : names) {
                first = delimit(first);
                handle(this::printDescription, component);
            }
        }
        return 0;
    }

    protected Integer handleTemplte() {
        return handle(this::printModel, exclusive.template.orElseThrow());
    }

    @Override
    protected Integer handleDescribe() {
        return handle(this::printDescription, exclusive.component.orElseThrow());
    }

    private int handle(BiFunction<ComponentService<ComponentContext, Model, Component>, Class<? extends Struct>, Integer> func, String name) {
        Map<String, SubstrateProvider> providers = showCommand.main.substratesOptions().requestedProvider();
        for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
            ComponentService<ComponentContext, Model, Component> componentService = entry.getValue().components().get(name);

            if (componentService != null) {
                return func.apply(componentService, componentService.modelClass());
            }
        }

        throw new IllegalArgumentException("no model found for: " + name);
    }

    protected int printModel(ComponentService<ComponentContext, Model, Component> componentService, Class<? extends Struct> modelClass) {
        showCommand.main.printer().println(getModel(modelClass));
        return 0;
    }

    protected int printDescription(ComponentService<ComponentContext, Model, Component> componentService, Class<? extends Struct> modelClass) {
        Class<?> componentClass = componentService.getClass();
        Optional<ProvidesComponent> providesComponent = Annotations.find(componentClass, ProvidesComponent.class);

        if (providesComponent.isEmpty()) {
            throw new IllegalStateException("component does not have a ProvidesComponent annotation: " + componentClass.getName());
        }

        String template = """
                Name:
                    {{name}}
                                    
                Synopsis:
                {{{indent synopsis 4}}}
                {{#description~}}
                Description:
                {{{indent description 4}}}
                {{/description~}}
                Template:
                {{{model}}}
                """;

        Map<Object, Object> params = Map.of(
                "name", providesComponent.get().type(),
                "synopsis", providesComponent.get().synopsis(),
                "description", providesComponent.get().description(),
                "model", getModel(modelClass)
        );

        write(template, params);
        return 0;
    }

    private void write(String template, Map<Object, Object> params) {
        try {
            Context context = Context
                    .newBuilder(params)
                    .resolver(
                            MapValueResolver.INSTANCE
                    )
                    .build();

            Handlebars handlebars = new Handlebars()
                    .prettyPrint(false);

            StringHelpers.register(handlebars);

            handlebars.registerHelper("indent", this::indent);

            Template compile = handlebars.compileInline(template);

            Writer writer = showCommand.main.printer().writer();
            compile.apply(context, writer);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected CharSequence indent(final Object value, final Options options) {
        Integer width = options.param(0, 4);
        notNull(width, "found 'null', expected 'indent'");
        return value.toString().trim().indent(width);
    }

    private boolean delimit(boolean first) {
        if (!first) {
            showCommand.main.printer().println("========================================");
        }

        return false;
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
