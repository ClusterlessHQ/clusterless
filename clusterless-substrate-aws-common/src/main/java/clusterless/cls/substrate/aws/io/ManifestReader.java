/*
 * Copyright (c) 2023-2025 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.substrate.aws.io;

import clusterless.cls.model.manifest.Manifest;

public class ManifestReader extends MetaReader<Manifest> {
    public ManifestReader() {
        super(Manifest.class);
    }
}
