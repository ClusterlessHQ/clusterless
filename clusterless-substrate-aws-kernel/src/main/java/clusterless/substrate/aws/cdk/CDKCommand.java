/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.substrate.aws.cdk;

import java.util.Optional;

public enum CDKCommand {
    DEPLOY,
    DESTROY,
    DIFF,
    VERIFY,
    SYNTH,
    IMPORT;

    public String command() {
        return name().toLowerCase();
    }

    public static CDKCommand from(String command) {
        return Optional.ofNullable(command).map(String::toUpperCase).map(CDKCommand::valueOf).orElse(null);
    }
}
