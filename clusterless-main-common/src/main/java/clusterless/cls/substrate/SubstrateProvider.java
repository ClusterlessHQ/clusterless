/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate;

import clusterless.cls.config.Configuration;
import clusterless.cls.managed.component.Component;
import clusterless.cls.managed.component.ComponentContext;
import clusterless.cls.managed.component.ComponentService;
import clusterless.cls.model.Model;
import clusterless.cls.model.Struct;

import java.util.Map;

/**
 *
 */
public interface SubstrateProvider {
    String providerName();

    int execute(String[] args);

    Map<String, ComponentService<ComponentContext, Model, Component>> components();

    Map<String, Class<? extends Struct>> models();

    Class<? extends Configuration> configClass();
}
