/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.naming;

import java.util.Locale;

public class Stage extends Fixed {
    private static final Stage NULL_STAGE = new Stage(null);

    public static Stage of(String stage) {
        return new Stage(stage == null ? null : stage.toUpperCase(Locale.ROOT));
    }

    public static Stage nullStage() {
        return NULL_STAGE;
    }

    protected Stage(String value) {
        super(value);
    }

    public Stage asLower() {
        if (isNull()) {
            return NULL_STAGE;
        }
        return new Stage(value.toLowerCase());
    }
}
