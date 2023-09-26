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
public class ProviderSubstratesOptions {
    public static ProviderSubstratesOptions ignored() {
        return new ProviderSubstratesOptions(true);
    }

    protected SubstrateProviders providers;

    @CommandLine.Option(
            names = {"-P", "--providers"},
            description = "Provider substrates to target.",
            scope = CommandLine.ScopeType.INHERIT
    )
    protected Set<String> providerNames = new LinkedHashSet<>();

    public ProviderSubstratesOptions() {
        this.providers = new SubstrateProviders();
        this.providerNames.addAll(providers.names());
    }

    private ProviderSubstratesOptions(boolean ignore) {
    }

    public Set<String> availableNames() {
        return providers.names();
    }

    public Set<String> providerNames() {
        return providerNames;
    }

    public Map<String, SubstrateProvider> requestedProvider() {
        Map<String, SubstrateProvider> result = new LinkedHashMap<>();

        providerNames.forEach(s -> result.put(s, providers.get(s)));

        return result;
    }
}
