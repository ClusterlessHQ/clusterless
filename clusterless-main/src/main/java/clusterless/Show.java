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
@CommandLine.Command(name = "show", subcommands = Show.ShowModel.class)
public class Show {
    @CommandLine.ParentCommand
    Main main;

    public Show() {
    }

    @CommandLine.Command(name = "providers", description = "show all available providers")
    public int providers() {

        main.printer().println(main.substratesOptions().available());

        return 0;
    }

    @CommandLine.Command(
            name = "model"
    )
    public static class ShowModel implements Callable<Integer> {

        static class Exclusive {
            @CommandLine.Option(names = "--list", arity = "0")
            Optional<Boolean> list;

            @CommandLine.Option(names = "--print", arity = "1")
            Optional<String> modelName;

            public Exclusive() {
            }
        }

        @CommandLine.ParentCommand
        Show show;

        @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;

        public ShowModel() {
        }

        @Override
        public Integer call() throws Exception {
            if (exclusive.list.isPresent() && exclusive.list.get()) {
                show.main.printer().println(Models.names());

                Map<String, SubstrateProvider> providers = show.main.substratesOptions().requestedSubstrates();

                for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
                    show.main.printer().println(entry.getValue().models().keySet());
                }
            } else if (exclusive.modelName.isPresent()) {
                Class<? extends Struct> modelClass = Models.get(exclusive.modelName.get());

                if (modelClass != null) {
                    return printModel(modelClass);
                }

                Map<String, SubstrateProvider> providers = show.main.substratesOptions().requestedSubstrates();
                for (Map.Entry<String, SubstrateProvider> entry : providers.entrySet()) {
                    modelClass = entry.getValue().models().get(exclusive.modelName.get());

                    if (modelClass != null) {
                        return printModel(modelClass);
                    }
                }

                throw new IllegalArgumentException("no model found for: " + exclusive.modelName.get());
            }

            return 0;
        }

        private int printModel(Class<? extends Struct> modelClass) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
            show.main.printer().println(JSONUtil.writeAsPrettyStringSafe(modelClass.getConstructor().newInstance()));
            return 0;
        }
    }

}
