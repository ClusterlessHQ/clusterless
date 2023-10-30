/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.show;

import clusterless.cls.managed.component.DocumentsModel;
import clusterless.cls.model.Struct;
import clusterless.cls.model.deploy.Models;
import clusterless.cls.util.Annotations;
import clusterless.cls.util.ExitCodeException;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;

import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@CommandLine.Command(
        name = "model",
        description = "Show model elements."
)
public class ShowModels extends BaseShowElements {

    public ShowModels() {
    }

    @Override
    @NotNull
    protected String elementType() {
        return "Models";
    }

    @Override
    protected Collection<String> getNames() {
        return Models.names();
    }

    protected int handle(String name, Handler func) {
        Class<? extends Struct> modelClass = Models.get(name);

        if (modelClass == null) {
            throw new ExitCodeException("no model found for: " + name, 1);
        }

        return func.handle(name, modelClass, modelClass);
    }

    protected void printDescriptionUsing(Class<?> documentedClass, Class<? extends Struct> modelClass, String template, Writer writer) {
        Optional<DocumentsModel> documentsModel = Annotations.find(documentedClass, DocumentsModel.class);

        if (documentsModel.isEmpty()) {
            throw new IllegalStateException("model does not have a DocumentsModel annotation: " + documentedClass.getName());
        }

        Map<String, Object> params = Map.of(
                "name", modelClass.getSimpleName(),
                "synopsis", documentsModel.get().synopsis(),
                "description", documentsModel.get().description(),
                "model", BaseShowElements.getModel(modelClass, false),
                "required", BaseShowElements.getModel(modelClass, true)
        );

        showCommand.main.printer().writeWithTemplate(template, params, writer);
    }
}
