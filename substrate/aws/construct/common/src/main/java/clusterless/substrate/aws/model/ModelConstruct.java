/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.model;

import clusterless.managed.Label;
import clusterless.model.Extensible;
import clusterless.model.Model;
import clusterless.substrate.aws.managed.ManagedComponentContext;
import clusterless.substrate.aws.managed.ManagedConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import software.constructs.IConstruct;

import java.util.function.Supplier;

/**
 *
 */
public class ModelConstruct<M extends Model> extends ManagedConstruct {
    private static final Logger LOG = LogManager.getLogger(ModelConstruct.class);

    private final M model;

    public ModelConstruct(@NotNull ManagedComponentContext context, @NotNull M model, @NotNull String id) {
        super(context, uniqueId(model, id));
        this.model = model;
    }

    private static Label uniqueId(@NotNull Model model, @NotNull String id) {
        return model.label()
                .with(Label.of(model.getClass().getSimpleName()))
                .with(Label.of(id));
    }

    public M model() {
        return model;
    }

    protected String id(String value) {
        return model()
                .label()
                .with(Label.of(value))
                .camelCase();
    }

    protected <R extends IConstruct> R construct(Supplier<R> supplier) {
        if (model() instanceof Extensible) {
            return construct(((Extensible) model()).type(), supplier);
        }
        return construct(null, supplier);
    }

    protected <R extends IConstruct> R construct(String expectedType, Supplier<R> supplier) {
        try {
            return supplier.get();
        } catch (software.amazon.jsii.JsiiException error) {
            String errorMessage = error.getMessage();

            if (Strings.isEmpty(expectedType)) {
                LOG.error("failed constructing object with: {}", errorMessage);
            } else {
                LOG.error("failed constructing: {}, with: {}", expectedType, errorMessage);
            }

            throw new IllegalStateException(errorMessage, error);
        }
    }
}
