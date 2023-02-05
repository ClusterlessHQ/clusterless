/*
 * Copyright (c) 2023 Chris K Wensel <chris@wensel.net>. All Rights Reserved.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package clusterless.util;

import com.google.common.base.Strings;

import java.net.URI;
import java.nio.file.Paths;

/**
 *
 */
public class URIs {
    /**
     * Returns a normalized path, that is multiple slashes and dots are removed.
     * <p>
     * Unlike Path#normalize, the last slash is retained.
     *
     * @param path
     * @return
     */
    public static String normalize(String path) {
        String empty = Strings.nullToEmpty(path);

        String result = Paths.get(empty).normalize().toString();

        if (result.length() != 1 && empty.endsWith("/")) {
            return result + "/";
        }

        return result;
    }

    public static String asPrefix(URI uri) {
        String normalize = uri.normalize().getPath();

        if (normalize.isEmpty()) {
            return null;
        }

        if (normalize.charAt(0) == '/') {
            if (normalize.length() == 1) {
                return null;
            }

            return normalize.substring(1);
        }

        return normalize;
    }
}
