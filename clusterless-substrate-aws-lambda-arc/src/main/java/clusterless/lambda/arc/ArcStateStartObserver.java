/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.arc;

import clusterless.lambda.EventObserver;
import clusterless.model.state.ArcState;

import java.util.List;

public interface ArcStateStartObserver extends EventObserver {
    void applyCurrentState(String lotId, ArcState currentState);

    void applyFinalArcStates(ArcState previous, ArcState current);

    void applyRoles(List<String> roles);
}
