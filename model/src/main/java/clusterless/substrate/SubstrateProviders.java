/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 */
public class SubstrateProviders {

    Map<String, SubstrateProvider> substrates;

    public SubstrateProviders() {
        ServiceLoader<SubstrateProvider> serviceLoader = ServiceLoader.load(SubstrateProvider.class);

        substrates = serviceLoader.stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toMap(SubstrateProvider::name, k -> k));

    }

    public SubstrateProvider get(String name) {
        return substrates.get(name);
    }

    public Map<String, SubstrateProvider> substrates() {
        return substrates;
    }

    public Set<String> names() {
        return substrates.keySet();
    }
}
