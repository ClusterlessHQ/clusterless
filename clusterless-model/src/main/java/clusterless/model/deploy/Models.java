/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model.deploy;

import clusterless.model.Struct;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convenience for identifying model types that should be printed out as templates.
 */
public class Models {
    public static final Map<String, Class<? extends Struct>> models = new LinkedHashMap<>();

    private static void add(Class<? extends Struct> type) {
        models.put(type.getSimpleName(), type);
    }

    static {
        add(Deployable.class);
        add(Arc.class);
        add(SourceDataset.class);
        add(SinkDataset.class);
    }

    public static Collection<String> names() {
        return models.keySet();
    }

    public static Class<? extends Struct> get(String name) {
        return models.get(name);
    }
}
