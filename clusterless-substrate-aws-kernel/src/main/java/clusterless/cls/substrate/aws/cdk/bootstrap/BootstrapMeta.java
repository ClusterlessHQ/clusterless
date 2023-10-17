/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.cdk.bootstrap;

import clusterless.cls.substrate.aws.scoped.ScopedMeta;

public class BootstrapMeta extends ScopedMeta {
    String version;

    public BootstrapMeta() {
    }

    public BootstrapMeta(String version) {
        this.version = version;
    }

    public String version() {
        return version;
    }

    public BootstrapMeta setVersion(String version) {
        this.version = version;
        return this;
    }
}
