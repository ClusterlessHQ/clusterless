/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.lambda.manifest;

/**
 *
 */
public class ManifestExistsException extends RuntimeException {
    public ManifestExistsException(String message) {
        super(message);
    }

    public ManifestExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ManifestExistsException(Throwable cause) {
        super(cause);
    }

    public ManifestExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
