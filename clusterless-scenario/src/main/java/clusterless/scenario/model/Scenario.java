/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.scenario.model;

import clusterless.config.Config;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class Scenario extends Config {

    boolean enabled = true;

    String name;
    String description;

    List<Path> projectFiles = new LinkedList<>();

    Path projectDirectory;

    List<IngressStore> ingressStores = new LinkedList<>();

    List<WatchedStore> watchedStores = new LinkedList<>();

    public boolean enabled() {
        return enabled;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    public Path projectDirectory() {
        return projectDirectory;
    }

    public List<Path> projectFiles() {
        return projectFiles;
    }

    public Scenario setProjectDirectory(Path projectDirectory) {
        this.projectDirectory = projectDirectory;
        return this;
    }

    public List<IngressStore> ingressStores() {
        return ingressStores;
    }

    public List<WatchedStore> watchedStores() {
        return watchedStores;
    }
}
