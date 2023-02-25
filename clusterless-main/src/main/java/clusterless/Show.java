/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless;

import clusterless.command.CommonCommandOptions;
import clusterless.json.JSONUtil;
import clusterless.model.Struct;
import clusterless.model.deploy.Models;
import clusterless.substrate.SubstrateProvider;
import picocli.CommandLine;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 *
 */
@CommandLine.Command(
        name = "show",
        description = "display details about providers and models",
        subcommands = {
                Show.ShowProviders.class,
                Show.ShowModel.class
        }
)
public class Show {
    @CommandLine.ParentCommand
    Main main;

    @CommandLine.Mixin
    CommonCommandOptions commandOptions = new CommonCommandOptions();

    public Show() {
    }

    public static class BaseShow implements Callable<Integer> {
        static class Exclusive {
            @CommandLine.Option(names = "--list", arity = "0")
            Optional<Boolean> list;

            @CommandLine.Option(names = "--print", arity = "1")
            Optional<String> name;

            public Exclusive() {
            }
        }

        @CommandLine.ParentCommand
        Show show;

        @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;

        @Override
        public Integer call() throws Exception {
            if (exclusive.list.isPresent() && exclusive.list.get()) {
                return handleList();
            } else if (exclusive.name.isPresent()) {
                return handleName();
            }

            return 0;
        }

        protected Integer handleName() throws Exception {
            return 0;
        }

        protected Integer handleList() throws Exception {
            return 0;
        }
    }

    @CommandLine.Command(name = "provider", description = "show all available providers")
    public static class ShowProviders extends BaseShow implements Callable<Integer> {

        @Override
        public Integer handleList() throws Exception {
            show.main.printer().println(show.main.substratesOptions().available());
            return 0;
        }
    }

    @CommandLine.Command(
            name = "model"
    )
    public static class ShowModel extends BaseShow {

        public ShowModel() {
        }

        protected Integer handleName() throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            Class<? extends Struct> modelClass = Models.get(exclusive.name.get());

            if (modelClass != null) {
                return printModel(modelClass);
            }

            Map<String, SubstrateProvider> providers = show.main.substratesOptions().requestedSubstrates();
            for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
                modelClass = entry.getValue().models().get(exclusive.name.get());

                if (modelClass != null) {
                    return printModel(modelClass);
                }
            }

            throw new IllegalArgumentException("no model found for: " + exclusive.name.get());
        }

        protected Integer handleList() {
            show.main.printer().println(Models.names());

            Map<String, SubstrateProvider> providers = show.main.substratesOptions().requestedSubstrates();

            for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
                show.main.printer().println(entry.getValue().models().keySet());
            }

            return 0;
        }

        private int printModel(Class<? extends Struct> modelClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            show.main.printer().println(JSONUtil.writeAsPrettyStringSafe(modelClass.getConstructor().newInstance()));
            return 0;
        }
    }

}
