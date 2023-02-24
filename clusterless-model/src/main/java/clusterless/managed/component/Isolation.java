/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.managed.component;

/**
 *
 */
public enum Isolation {
    /**
     * A container provider returns a resource that creates a new stack
     */
    isolated,
    /**
     * A member provider returns a resource included in a managed stack
     */
    included,
    embedded
}
