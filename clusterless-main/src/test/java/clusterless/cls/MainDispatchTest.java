/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls;

import clusterless.cls.config.Configuration;
import clusterless.cls.managed.component.Component;
import clusterless.cls.managed.component.ComponentContext;
import clusterless.cls.managed.component.ComponentService;
import clusterless.cls.model.Model;
import clusterless.cls.model.Struct;
import clusterless.cls.substrate.ProviderSubstratesOptions;
import clusterless.cls.substrate.SubstrateProvider;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Main hands the raw argv to each declared provider, and every provider run reads all the project
 * files, so a provider declared by several project files must still execute exactly once.
 */
class MainDispatchTest {
    private final List<String> executed = new ArrayList<>();
    private final String[] args = {"deploy", "-p", "a.json,b.json"};

    @Test
    void sameProviderInSeveralFilesExecutesOnce() {
        Main main = mainWith(Map.of("aws", 0));

        int result = main.run(List.of("aws", "aws"), args);

        assertEquals(0, result);
        assertEquals(List.of("aws"), executed);
    }

    @Test
    void distinctProvidersExecuteOnceEachInDeclaredOrder() {
        Map<String, Integer> exits = new LinkedHashMap<>();
        exits.put("b", 0);
        exits.put("a", 0);
        Main main = mainWith(exits);

        int result = main.run(List.of("a", "b", "a"), args);

        assertEquals(0, result);
        assertEquals(List.of("a", "b"), executed);
    }

    @Test
    void undeclaredProviderStillFails() {
        Main main = mainWith(Map.of("aws", 0));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> main.run(List.of("aws", "gcp"), args));

        assertEquals("substrate not found: gcp", exception.getMessage());
        assertEquals(List.of("aws"), executed);
    }

    @Test
    void nonZeroExitStopsDispatch() {
        Map<String, Integer> exits = new LinkedHashMap<>();
        exits.put("a", 3);
        exits.put("b", 0);
        Main main = mainWith(exits);

        int result = main.run(List.of("a", "b", "a"), args);

        assertEquals(3, result);
        assertEquals(List.of("a"), executed);
    }

    private Main mainWith(Map<String, Integer> exits) {
        Map<String, SubstrateProvider> stubs = new LinkedHashMap<>();

        exits.forEach((name, exit) -> stubs.put(name, new RecordingProvider(name, exit)));

        ProviderSubstratesOptions options = new ProviderSubstratesOptions() {
            @Override
            public Map<String, SubstrateProvider> requestedProvider() {
                return stubs;
            }
        };

        return new Main(args) {
            @Override
            public ProviderSubstratesOptions substratesOptions() {
                return options;
            }
        };
    }

    private class RecordingProvider implements SubstrateProvider {
        private final String name;
        private final int exit;

        RecordingProvider(String name, int exit) {
            this.name = name;
            this.exit = exit;
        }

        @Override
        public String providerName() {
            return name;
        }

        @Override
        public int execute(String[] args) {
            assertSame(MainDispatchTest.this.args, args);
            executed.add(name);
            return exit;
        }

        @Override
        public Map<String, ComponentService<ComponentContext, Model, Component>> components() {
            return Map.of();
        }

        @Override
        public Map<String, Class<? extends Struct>> models() {
            return Map.of();
        }

        @Override
        public Class<? extends Configuration> configClass() {
            return Configuration.class;
        }
    }
}
