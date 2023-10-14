/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.managed.component;

/**
 *
 */
public enum Isolation {
    /**
     * An independent provider returns a resource that creates a new stack
     */
    independent,
    /**
     * A grouped provider returns a resource grouped into a common support stack
     */
    grouped,

    /**
     * A managed provider is embedded in a unique stack that manages the constructs
     */
    managed
}
