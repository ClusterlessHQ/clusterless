/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate;

import picocli.CommandLine;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class SubstratesOptions {
    protected final SubstrateProviders providers = new SubstrateProviders();

    @CommandLine.Parameters
    protected String[] allArgs = new String[0];

    @CommandLine.Option(names = {"-s", "--substrates"}, description = "substrates to target", scope = CommandLine.ScopeType.INHERIT)
    protected Set<String> substrates = new LinkedHashSet<>();

    public SubstratesOptions() {
        this.substrates.addAll(providers.names());
    }

    public String[] allArgs() {
        return allArgs;
    }

    public Set<String> substrates() {
        return substrates;
    }

    public Map<String, SubstrateProvider> requestedSubstrates() {
        Map<String, SubstrateProvider> result = new LinkedHashMap<>();

        substrates.forEach(s -> result.put(s, providers.get(s)));

        return result;
    }
}
