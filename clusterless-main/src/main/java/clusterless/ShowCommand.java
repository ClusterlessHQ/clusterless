/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless;

import clusterless.command.CommonCommandOptions;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 *
 */
@CommandLine.Command(
        name = "show",
        description = "display details about providers, components, and project models",
        subcommands = {
                ShowProviders.class,
                ShowComponents.class,
                ShowModels.class
        }
)
public class ShowCommand {
    @CommandLine.ParentCommand
    Main main;

    @CommandLine.Mixin
    CommonCommandOptions commandOptions = new CommonCommandOptions();

    public ShowCommand() {
    }

    public static class BaseShow implements Callable<Integer> {
        static class Exclusive {
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @CommandLine.Option(
                    names = "--list",
                    arity = "0",
                    description = "list all names"
            )
            Optional<Boolean> list;

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @CommandLine.Option(
                    names = "--describe-all",
                    arity = "0",
                    description = "print description of all elements"
            )
            Optional<Boolean> all;

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @CommandLine.Option(
                    names = "--model",
                    arity = "1",
                    description = "print the json template of element"
            )
            Optional<String> model;

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @CommandLine.Option(names = "--describe", arity = "1")
            Optional<String> name;

            public Exclusive() {
            }
        }

        @CommandLine.ParentCommand
        ShowCommand showCommand;

        @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;

        @Override
        public Integer call() throws Exception {
            if (exclusive.all.isPresent() && exclusive.all.get()) {
                return handleDescribeAll();
            } else if (exclusive.list.isPresent() && exclusive.list.get()) {
                return handleList();
            } else if (exclusive.model.isPresent()) {
                return handleModel();
            } else if (exclusive.name.isPresent()) {
                return handleDescribe();
            }

            return 0;
        }

        protected Integer handleList() throws Exception {
            return 0;
        }

        protected Integer handleModel() throws Exception {
            return 0;
        }

        protected Integer handleDescribeAll() throws Exception {
            return 0;
        }

        protected Integer handleDescribe() throws Exception {
            return 0;
        }
    }
}
