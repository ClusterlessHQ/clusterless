/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model;

import clusterless.model.deploy.Deployable;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DeployableLoader extends Loader {
    public DeployableLoader(List<File> projectFiles) throws IOException {
        super(projectFiles);
    }

    public List<Deployable> readObjects(String provider) {
        return super.readObjects(provider, Deployable.PROVIDER_POINTER, Deployable.class, Deployable::setSourceFile);
    }
}
