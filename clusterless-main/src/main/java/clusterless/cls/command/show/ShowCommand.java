/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.command.show;

import clusterless.cls.Main;
import picocli.CommandLine;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 *
 */
@CommandLine.Command(
        name = "show",
        description = "Display details about providers, components, and project models.",
        subcommands = {
                ShowProviders.class,
                ShowComponents.class,
                ShowResources.class,
                ShowBoundaries.class,
                ShowBarriers.class,
                ShowArcs.class,
                ShowActivities.class,
                ShowModels.class,
                CommandLine.HelpCommand.class
        }
)
public class ShowCommand {
    @CommandLine.ParentCommand
    Main main;

    public ShowCommand() {
    }

    public static class BaseShow implements Callable<Integer> {
        static class Exclusive {
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @CommandLine.Option(
                    names = "--list",
                    arity = "0",
                    description = "List all names."
            )
            Optional<Boolean> list;

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @CommandLine.Option(
                    names = "--model",
                    arity = "1",
                    description = "Print the json template of element."
            )
            Optional<String> model;

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @CommandLine.Option(names = "--describe", arity = "1")
            Optional<String> name;

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @CommandLine.Option(
                    names = "--describe-all",
                    arity = "0",
                    description = "Print description of all elements."
            )
            Optional<Boolean> describeAll;

            @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            @CommandLine.Option(
                    names = "--model-all",
                    arity = "0",
                    description = "Print model of all elements."
            )
            Optional<Boolean> modelAll;

            public Exclusive() {
            }
        }

        @CommandLine.ParentCommand
        ShowCommand showCommand;

        @CommandLine.ArgGroup(exclusive = true, multiplicity = "1")
        Exclusive exclusive;

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        @CommandLine.Option(
                names = "--required",
                arity = "0",
                description = "Only show required fields in model."
        )
        Optional<Boolean> required;

        public Optional<Boolean> required() {
            return required;
        }

        @Override
        public Integer call() throws Exception {
            if (exclusive.describeAll.isPresent() && exclusive.describeAll.get()) {
                return handleDescribeAll();
            } else if (exclusive.modelAll.isPresent() && exclusive.modelAll.get()) {
                return handleModelAll();
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

        protected Integer handleModelAll() throws Exception {
            return 0;
        }

        protected Integer handleDescribe() throws Exception {
            return 0;
        }
    }
}


