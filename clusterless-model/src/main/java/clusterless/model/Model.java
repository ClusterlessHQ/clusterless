/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.model;

import clusterless.json.JSONUtil;
import clusterless.naming.Label;

public abstract class Model implements Struct {
    public final Label label() {
        return Label.of(getClass().getSimpleName());
    }

    @Override
    public final String toString() {
        return JSONUtil.writeAsStringSafe(this);
    }
}
