/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.cls.util;

public class ExitCodeException extends RuntimeException {
    private final int exitCode;

    public ExitCodeException(int exitCode) {
        this.exitCode = exitCode;
    }

    public ExitCodeException(String message, int exitCode) {
        super(message);
        this.exitCode = exitCode;
    }

    public ExitCodeException(String message, Throwable cause, int exitCode) {
        super(message, cause);
        this.exitCode = exitCode;
    }

    public int exitCode() {
        return exitCode;
    }
}
